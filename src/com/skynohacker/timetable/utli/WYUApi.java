package com.skynohacker.timetable.utli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.util.Log;

public class WYUApi {
	private String mUserID;
	private String mUserPwd;
	private static String mHtml = null;
	private static String mRecords = null;
	private CookieStore mCookieStore;
	private boolean isWap;

	/**
	 * 
	 * @param userid
	 *            学号
	 * @param userpwd
	 *            密码
	 */
	public WYUApi(String userid, String userpwd) {
		mUserID = userid;
		mUserPwd = userpwd;
	}

	/**
	 * 判断网络是否可用并设置是否为wap网络
	 * 
	 * @param context
	 *            Context
	 * @return 如果网络可用返回true, 否则返回false
	 */
	public boolean isNetworkAvailable(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();

		if (info == null)
			return false;

		String typeName = info.getTypeName();
		Log.v("NetworkType", typeName);
		// Log.v("NetworkType", info.getExtraInfo());
		if (typeName == "MOBILE" && info.getExtraInfo().equals("cmwap")) {
			isWap = true;
		} else {
			isWap = false;
		}

		return true;
	}

	/***
	 * 登陆系统
	 * 
	 * @param context
	 *            Context
	 * @return 如果登陆成功返回true, 否则返回false
	 */
	public boolean login(Context context) throws ClientProtocolException,
			IOException {
		// 获取代理信息
		String host = Proxy.getDefaultHost();
		int port = Proxy.getDefaultPort();
		Log.v("proxy", "host=" + host);
		Log.v("proxy", "port" + port);

		// 先请求验证码页面
		HttpClient httpClient = new DefaultHttpClient();
		// 设置连接超时时间
		httpClient.getParams().setIntParameter("http.socket.timeout", 10000);
		HttpGet httpGet = new HttpGet("http://jwc.wyu.edu.cn/student/rndnum.asp");
		httpClient.execute(httpGet);
		httpClient.getConnectionManager().shutdown();
		// 获取cookie
		mCookieStore = ((DefaultHttpClient) httpClient).getCookieStore();
		List<Cookie> cookies = mCookieStore.getCookies();

		// 查找验证码的cookie在list的位置
		int pos = 0;
		for (int i = 0; i < cookies.size(); i++) {
			if (cookies.get(i).getName().equals("LogonNumber")) {
				pos = i;
			}
			Log.v("cookies", cookies.get(i).getName() + ": " + cookies.get(i).getValue());
		}
		Log.v("验证码","" +cookies.get(pos));
		Log.v("用户名", mUserID);
		Log.v("密码", mUserPwd);
		// 构建post报头
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("UserCode", mUserID));
		params.add(new BasicNameValuePair("UserPwd", mUserPwd));
		params.add(new BasicNameValuePair("Validate", cookies.get(pos)
				.getValue().toString()));

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");

		// 设置httppost
		HttpPost httpPost = new HttpPost(
				"http://jwc.wyu.edu.cn/student/logon.asp");
		httpPost.setEntity(entity);

		//设置httpClient参数，不自动重定向
		HttpParams httpParams = new BasicHttpParams();
		HttpClientParams.setRedirecting(httpParams, false);

		DefaultHttpClient httpClient2 = new DefaultHttpClient(httpParams);
		// 设置连接超时
		httpClient2.getParams().setIntParameter("http.socket.timeout", 10000);
		httpClient2.setCookieStore(mCookieStore);


		HttpContext localContext = new BasicHttpContext();

		// 登录
		HttpResponse response = httpClient2.execute(httpPost, localContext);
		HttpEntity loginEntity = response.getEntity();
		if (loginEntity != null) {
			String str = new String(EntityUtils.toString(loginEntity).getBytes(
					"ISO-8859-1"), "GB2312");
			Log.v("html_size", "" + str.length());
			Log.v("html_content", str);
		}
		httpClient2.getConnectionManager().shutdown();
		Log.v("response", response.getStatusLine().toString());

		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			// 登录失败
			Log.v("return", "false");
			return false;
		}
		return true;
	}

	public List<ClassInfo> getTimetable() throws IOException {
		// 获取代理信息
		String host = Proxy.getDefaultHost();
		int port = Proxy.getDefaultPort();
		Log.v("proxy", "host=" + host);
		Log.v("proxy", "port" + port);

		// 请求课程表(f3.app)页面
		HttpGet httpGet = new HttpGet("http://jwc.wyu.edu.cn/student/f3.asp");

		HttpClient httpClient = new DefaultHttpClient();
		// 设置超时
		httpClient.getParams().setIntParameter("http.socket.timeout", 20000);
		((DefaultHttpClient) httpClient).setCookieStore(mCookieStore);

		HttpResponse response;
		try {
			response = httpClient.execute(httpGet);
			Log.v("response3", response.getStatusLine().toString());
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				mHtml = new String(EntityUtils.toString(entity).getBytes(
						"ISO-8859-1"), "GB2312");
				Log.v("html_size", "" + mHtml.length());
				// Log.v("html_content", mHtml);
			} else {
				return null;
			}
		} catch (ClientProtocolException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally{
			httpClient.getConnectionManager().shutdown();
		}
		if (mHtml.contains("top.location.href"))
			return null;
		else
			return WYUParser.parseTimetable(mHtml);
		
	}

	// 下载数据
	public boolean downloadData(Context context)
			throws ClientProtocolException, IOException {
		// 获取代理信息
		String host = Proxy.getDefaultHost();
		int port = Proxy.getDefaultPort();
		Log.v("proxy", "host=" + host);
		Log.v("proxy", "port" + port);

		// 请求f3.app页面
		HttpGet httpGet2 = new HttpGet("http://jwc.wyu.edu.cn/student/f3.asp");

		HttpClient httpClient3 = new DefaultHttpClient();
		// 设置超时
		httpClient3.getParams().setIntParameter("http.socket.timeout", 20000);
		((DefaultHttpClient) httpClient3).setCookieStore(mCookieStore);
		if (isWap) {
			// HttpHost httpHost = new HttpHost(host, port);
			HttpHost httpHost = new HttpHost("10.0.0.172", 80, "http");
			httpClient3.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY,
					httpHost);
		}

		HttpResponse response = httpClient3.execute(httpGet2);
		Log.v("response3", response.getStatusLine().toString());
		HttpEntity entity2 = response.getEntity();
		if (entity2 != null) {
			mHtml = new String(EntityUtils.toString(entity2).getBytes(
					"ISO-8859-1"), "GB2312");
			Log.v("html_size", "" + mHtml.length());
			// Log.v("html_content", mHtml);
			httpClient3.getConnectionManager().shutdown(); // 关闭httpclient并释放资源
		} else {
			httpClient3.getConnectionManager().shutdown();
			return false;
		}

		// 请求f4.asp
		HttpGet httpGet3 = new HttpGet("http://jwc.wyu.edu.cn/student/f4.asp");
		HttpClient httpClient4 = new DefaultHttpClient();

		// 设置超时
		httpClient4.getParams().setIntParameter("http.socket.timeout", 400000);
		((DefaultHttpClient) httpClient4).setCookieStore(mCookieStore);
		if (isWap) {
			HttpHost httpHost = new HttpHost("10.0.0.172", 80, "http");
			httpClient4.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY,
					httpHost);
		}

		HttpResponse response2 = httpClient4.execute(httpGet3);
		Log.v("response4", response2.getStatusLine().toString());
		HttpEntity entity3 = response2.getEntity();
		if (entity3 != null) {
			mRecords = new String(EntityUtils.toString(entity3).getBytes(
					"ISO-8859-1"), "GB2312");
			// Log.v("html_content", mRecords);
			// Log.v("html_size", "" + mRecords.length());
			httpClient4.getConnectionManager().shutdown();
		} else {
			httpClient4.getConnectionManager().shutdown();
			return false;
		}

		return true;
	}

	// 返回课程表html文档
	public String getHtml() {
		return mHtml;
	}

	// 返回成绩表html文档
	public String getRecords() {
		return mRecords;
	}
}

