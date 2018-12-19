package viroyal.com.dev.splash;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.suntiago.baseui.utils.log.Slog;
import com.suntiago.network.network.Api;
import com.suntiago.network.network.BaseRspObserver;
import com.suntiago.network.network.rsp.BaseResponse;
import com.suntiago.network.network.utils.MacUtil;
import com.suntiago.network.network.utils.SPUtils;

import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Jeremy on 2018/8/2.
 */

public class ConfigDevice {
    private final static String TAG = "ConfigDevice";
    private static String GETAPI_URL_MCPAPI = "";
    public static String school_id = "";
    public static String operator = "";

    //获取设备ip地址
    public static String getDeviceId(Context context) {
        return MacUtil.getLocalMacAddressFromIp();
    }

    //获取设备ip地址
    public static String getDeviceId() {
        return MacUtil.getLocalMacAddressFromIp();
    }

    public void setGetapiUrl(String url){
        this.GETAPI_URL_MCPAPI = url;
    }

    //配置ip地址
    public static Subscription configIp(final Context context, String type, final Action1<BaseResponse> action) {
        return Api.get().getApi(IpConfig.class, GETAPI_URL_MCPAPI)
                .api(ConfigDevice.getDeviceId(context), "JianKong")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseRspObserver<ApiResponse>(ApiResponse.class, new Action1<ApiResponse>() {
                    @Override
                    public void call(ApiResponse rsp) {
                        if (rsp.error_code == 1000) {
                            Slog.d(TAG, "call [rsp]:" + rsp.apiModel.config);
                            if (!SPUtils.getInstance(context).get("api_config").equals(rsp.apiModel.config)) {
                                Gson gson = new Gson();
                                ApiConfig ac = gson.fromJson(rsp.apiModel.config, ApiConfig.class);

                                SPUtils.getInstance(context).put("api_config", rsp.apiModel.config);
                                if (!TextUtils.isEmpty(rsp.apiModel.school_id)) {
                                    SPUtils.getInstance(context).put("school_id", rsp.apiModel.school_id);
                                    school_id = rsp.apiModel.school_id;
                                }
                                if (!TextUtils.isEmpty(ac.school_id)) {
                                    SPUtils.getInstance(context).put("school_id", ac.school_id);
                                    school_id = ac.school_id;
                                }
                                Slog.d(TAG, "call [rsp]:school_id:" + ac.school_id);
                                Api.get().setApiConfig(ac.api + "/", ac.netty_host, ac.netty_port);
                            }
                        }
                        if (action != null) {
                            action.call(rsp);
                        }
                    }
                }));
    }

    interface IpConfig {
        /**
         * 获取api
         *
         * @param mac
         * @param app_name
         * @return
         */
        @GET("device/devmonitor/dev/api-config")
        Observable<ApiResponse> api(@Header("dev_mac") String mac, @Query("app_name") String app_name);
    }
}
