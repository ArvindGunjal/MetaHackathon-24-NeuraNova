package com.meta.hackathon.util;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.meta.hackathon.config.ReloadableProperties;
import com.meta.hackathon.http.HTTPResponse;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttpUtils {

	private static final int MAX_REQUESTS_PER_HOST = ReloadableProperties.getOkHttpMaxRequestsPerHost();
	private static final int MAX_REQUESTS_PER_CLIENT = 200;
	private static OkHttpClient client = null;
	private static boolean ignoreSslCertificate = true;
	public static final int DEFAULT_RETRYCOUNT = 3;
	private static final Logger LOG = LogManager.getLogger(OkHttpUtils.class.getName());
	public static int RETRY_COUNT = 1;
	private static Consumer<HTTPResponse> errorConsumer = (response) -> {
		// SMSUtils.log("Error:" + response.getBodyAsString() + ", Response: " +
		// response);
	};

	private static ConnectionPool connectionPool;

	public static void setErrorConsumer(Consumer<HTTPResponse> errorConsumer) {
		OkHttpUtils.errorConsumer = errorConsumer;
	}

	private OkHttpUtils() {
	}

	public static OkHttpClient getClient() {

		if (client == null) {
			synchronized (OkHttpUtils.class) {
				if (client == null) {
					try {
						init(ignoreSslCertificate);
					} catch (Exception e) {
						LOG.error("Error", e);
					}
				}
			}
		}
		return client;
	}

	public static void init(boolean ignoreCertificate) {
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		LOG.debug("Initializing httpUtil with default configuration");
		// Connection pool setting
		// TODO : make this configurable
		Dispatcher dispatcher = new Dispatcher();
		dispatcher.setMaxRequests(MAX_REQUESTS_PER_CLIENT);
		dispatcher.setMaxRequestsPerHost(MAX_REQUESTS_PER_HOST);
		ConnectionPool pool = new ConnectionPool(100, 60, TimeUnit.SECONDS);
		builder.connectTimeout(ReloadableProperties.getOkHttpConnectTimeoutInSeconds(), TimeUnit.SECONDS);
		builder.readTimeout(ReloadableProperties.getOkHttpReadTimeoutInSeconds(), TimeUnit.SECONDS);
		builder.writeTimeout(ReloadableProperties.getOkHttpWriteTimeoutInSeconds(), TimeUnit.SECONDS);
		builder.retryOnConnectionFailure(false);
		builder.connectionPool(pool);
		builder.dispatcher(dispatcher);
		if (ignoreCertificate) {
			OkHttpUtils.ignoreSslCertificate = true;
			builder = configureToIgnoreCertificate(builder);
		}
		client = builder.build();
		connectionPool = client.connectionPool();
		/*
		 * new Thread(new Runnable() { public void run() { Timer timer = new
		 * Timer("OK_HTTP_POOL_STATS"); timer.scheduleAtFixedRate(new TimerTask() {
		 * 
		 * @Override public void run() { //LOGGER.info("OKHTTP | connectionCount : {}" +
		 * connectionPool.connectionCount());
		 * //LOGGER.info("OKHTTP | idleConnectionCount : {}" +
		 * connectionPool.idleConnectionCount()); } }, 0, 10_000); Timer timer2 = new
		 * Timer("OK_HTTP_POOL_STATS"); timer2.scheduleAtFixedRate(new TimerTask() {
		 * 
		 * @Override public void run() { //LOGGER.info("OKHTTP | connectionCount : {}" +
		 * connectionPool.connectionCount());
		 * //LOGGER.info("OKHTTP | idleConnectionCount : {}" +
		 * connectionPool.idleConnectionCount()); } }, 0, 60_000); } },
		 * "OKHTTP_POOL_STATS").start();
		 */
	}

	// Setting testMode configuration. If set as testMode, the connection will skip
	// certification check
	public static OkHttpClient.Builder configureToIgnoreCertificate(OkHttpClient.Builder builder) {
		LOG.debug("Ignore Ssl Certificate");
		try {

			// Create a trust manager that does not validate certificate chains
			final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) // NOSONAR
						throws CertificateException {
				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) // NOSONAR
						throws CertificateException {
				}

				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return new java.security.cert.X509Certificate[] {};
				}
			} };

			// Install the all-trusting trust manager
			final SSLContext sslContext = SSLContext.getInstance("SSL"); // NOSONAR
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			// Create an ssl socket factory with our all-trusting manager
			final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

			builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
			builder.hostnameVerifier((hostname, session) -> true); // NOSONAR
		} catch (Exception e) {
			LOG.error("Exception while configuring IgnoreSslCertificate" + e, e);
		}
		return builder;
	}

	/**
	 * Execute request with retryOnFail as true by default
	 * 
	 * @param request
	 * @return
	 */
	public static HTTPResponse executeRequest(Request request) {
		return executeRequest(request, true);
	}

	public static HTTPResponse executeRequest(Request request, boolean retryOnFail) {
		if (retryOnFail) {
			return executeRequest(request, DEFAULT_RETRYCOUNT);
		} else {
			return executeRequest(request, 0);
		}
	}

	public static HTTPResponse executeRequest(Request request, int retryCount) {
		long startTs = System.currentTimeMillis();
		Response clonedResponse = null;
		try (Response response = getClient().newCall(request).execute()) {
			if (response.isSuccessful()) {
				try (ResponseBody responseBody = response.body()) {
					byte[] body = responseBody.bytes();
					return HTTPResponse.createSuccessResponse(response, body);
				}
			} else {
				clonedResponse = HTTPResponse.cloneResponse(response);
			}
		} catch (final Exception e) {
			clonedResponse = HTTPResponse.errorResponse(e, request, startTs);
		}
		retryCount--;
		if (retryCount <= 0) {
			HTTPResponse responseObject = HTTPResponse.createFailedResponse(clonedResponse);
			errorConsumer.accept(responseObject);
			return responseObject;
		}
		return executeRequest(request, retryCount);
	}

	public static HTTPResponse executeRequest(String uuid, Request request, int retryCount, long retryInterval) {
		long startTs = System.currentTimeMillis();
		Response clonedResponse = null;
		try (Response response = getClient().newCall(request).execute()) {
			if (response.isSuccessful()) {
				try (ResponseBody responseBody = response.body()) {
					byte[] body = responseBody.bytes();
					return HTTPResponse.createSuccessResponse(response, body);
				}
			} else {
				clonedResponse = HTTPResponse.cloneResponse(response);
			}
		} catch (final Exception e) {
			clonedResponse = HTTPResponse.errorResponse(e, request, startTs);
		}
		HTTPResponse responseObject = HTTPResponse.createFailedResponse(clonedResponse);
		errorConsumer.accept(responseObject);
		return responseObject;
	}

	public static void asyncRequest(Request request, String uuid, String url) {
		try {

			Instant requestTime = Instant.now();
			Call call = getClient().newCall(request);
			call.enqueue(new Callback() {

				@Override
				public void onFailure(Call call, IOException e) {
					LOG.error("{}::Async Callback failed for request {} with Exception {}", uuid, url, e);
				}

				@Override
				public void onResponse(Call call, Response response) throws IOException {

					HTTPResponse responseStr;
					if (response.isSuccessful()) {
						try (ResponseBody responseBody = response.body()) {
							byte[] body = responseBody.bytes();
							responseStr = HTTPResponse.createSuccessResponse(response, body);
							Instant responseTime = Instant.now();
							LOG.info(
									"{}::Async Callback response after forwarding to {} => Code:{} => ResponseBody:{}=>Timetaken:{} ms ",
									uuid, url, responseStr.getResponse().code(), responseStr.getBodyAsString(),
									Duration.between(requestTime, responseTime).toMillis());
						}
					} else {
						responseStr = HTTPResponse.createFailedResponse(response);
						Instant responseTime = Instant.now();
						LOG.info(
								"{}::Async Callback response after forwarding to {} => Code:{} => ResponseBody:{}=>Timetaken:{} ms ",
								uuid, url, responseStr.getResponse().code(), responseStr.getBodyAsString(),
								Duration.between(requestTime, responseTime).toMillis());
						// call.clone().enqueue(this);
					}

				}
			});
		} catch (Exception e) {
			LOG.error("{}::Exception while calling url {}::{} ", uuid, url, e);
		}

	}
}
