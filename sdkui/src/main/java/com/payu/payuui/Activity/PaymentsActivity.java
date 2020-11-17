package com.payu.payuui.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import android.webkit.WebView;

import com.payu.custombrowser.Bank;
import com.payu.custombrowser.CustomBrowser;
import com.payu.custombrowser.PackageListDialogFragment;
import com.payu.custombrowser.PayUCustomBrowserCallback;
import com.payu.custombrowser.PayUWebChromeClient;
import com.payu.custombrowser.bean.CustomBrowserConfig;
//import com.payu.custombrowser.upiintent.Payment;
import com.payu.india.Extras.PayUChecksum;
import com.payu.india.Model.PayuConfig;
//import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.paymentparamhelper.PostData;
import com.payu.phonepe.PhonePe;
import com.payu.phonepe.callbacks.PayUPhonePeCallback;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PaymentsActivity extends FragmentActivity {
    private Bundle bundle;
    private String url;
    private PayuConfig payuConfig;
    private String UTF = "UTF-8";
    private boolean viewPortWide = false;
    private String merchantHash;
    private String txnId = null;
    private String merchantKey;
    private PayUChecksum checksum;
    private String salt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //android O fix bug orientation
        if (android.os.Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (savedInstanceState == null) { ;
            bundle = getIntent().getExtras();

            PhonePe phonePe = PhonePe.getInstance();


          /*  if (bundle != null) {

                isStandAlonePhonePayAvailable = bundle.getBoolean("isPhonePeSupported", false);
              //  isPaymentByPhonePe = bundle.getBoolean("isPaymentByPhonePe", false);
                salt = bundle.getString(PayuConstants.SALT);
            }*/


            if (bundle != null)
                payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);

            if (payuConfig != null) {
                url = payuConfig.getEnvironment() == PayuConstants.PRODUCTION_ENV ? PayuConstants.PRODUCTION_PAYMENT_URL : PayuConstants.TEST_PAYMENT_URL;
              //  url="https://mobiletest.payu.in/_payment";
                String[] list=null;
                if(payuConfig.getData()!=null)
                list = payuConfig.getData().split("&");

                if(list != null) {
                    for (String item : list) {
                        String[] items = item.split("=");
                        if (items.length >= 2) {
                            String id = items[0];
                            switch (id) {
                                case "txnid":
                                    txnId = items[1];
                                    break;
                                case "key":
                                    merchantKey = items[1];
                                    break;
                                case "pg":
                                    if (items[1].contentEquals("NB")) {
                                        viewPortWide = true;
                                    }
                                    break;

                            }
                        }
                    }
                }


                PayUPhonePeCallback payUPhonePeCallback = new PayUPhonePeCallback() {

                    // Called when Payment gets Successful
                    @Override
                    public void onPaymentOptionSuccess(String payuResponse) {

                        Intent intent = new Intent();
                        intent.putExtra("payu_response", payuResponse);
                        setResult(PayuConstants.PAYU_REQUEST_CODE,intent);
                        finish();

                    }


                    // Called when Payment is failed
                    @Override
                    public void onPaymentOptionFailure(String payuResponse) {
                        Intent intent = new Intent();
                        intent.putExtra("payu_response", payuResponse);
                        setResult(PayuConstants.PAYU_REQUEST_CODE,intent);
                        finish();
                    }
                };

                //set callback to track important events
                PayUCustomBrowserCallback payUCustomBrowserCallback = new PayUCustomBrowserCallback() {



                    public void onVpaEntered(String vpa, PackageListDialogFragment packageListDialogFragment) {

                        //This hash should be generated from server

                        String input = "smsplus"+"|validateVPA|"+vpa+"|"+"1b1b0";

                         String verifyVpaHash = calculateHash(input.toString()).getResult();

                        packageListDialogFragment.verifyVpa(verifyVpaHash);


                    }

                    private PostData calculateHash(String hashString) {
                        try {
                            StringBuilder hash = new StringBuilder();
                            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
                            messageDigest.update(hashString.getBytes());
                            byte[] mdbytes = messageDigest.digest();
                            for (byte hashByte : mdbytes) {
                                hash.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
                            }

                            return getReturnData(PayuErrors.NO_ERROR, PayuConstants.SUCCESS, hash.toString());
                        } catch (NoSuchAlgorithmException e) {
                            return getReturnData(PayuErrors.NO_SUCH_ALGORITHM_EXCEPTION, PayuErrors.INVALID_ALGORITHM_SHA);
                        }
                    }

                    protected PostData getReturnData(int code, String status, String result) {
                        PostData postData = new PostData();
                        postData.setCode(code);
                        postData.setStatus(status);
                        postData.setResult(result);
                        return postData;
                    }

                    protected PostData getReturnData(int code, String result) {
                        return getReturnData(code, PayuConstants.ERROR, result);
                    }

                    /**
                     * This method will be called after a failed transaction.
                     *
                     * @param payuResponse     response sent by PayU in App
                     * @param merchantResponse response received from Furl
                     */
                    @Override
                    public void onPaymentFailure(String payuResponse, String merchantResponse) {
                        Intent intent = new Intent();
                        intent.putExtra("result", merchantResponse);
                        intent.putExtra("payu_response", payuResponse);
                        if (null != merchantHash) {
                            intent.putExtra(PayuConstants.MERCHANT_HASH, merchantHash);
                        }
                        setResult(Activity.RESULT_CANCELED, intent);
                        finish();
                    }

                    @Override
                    public void onPaymentTerminate() {
                        finish();
                    }

                    /**
                     * This method will be called after a successful transaction.
                     *
                     * @param payuResponse     response sent by PayU in App
                     * @param merchantResponse response received from Furl
                     */
                    @Override
                    public void onPaymentSuccess(String payuResponse, String merchantResponse) {
                        Intent intent = new Intent();
                        intent.putExtra("result", merchantResponse);
                        intent.putExtra("payu_response", payuResponse);
                        if (null != merchantHash) {
                            intent.putExtra(PayuConstants.MERCHANT_HASH, merchantHash);
                        }
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }

                    @Override
                    public void onCBErrorReceived(int code, String errormsg) {
                    }

                    @Override
                    public void setCBProperties(WebView webview, Bank payUCustomBrowser) {
                        webview.setWebChromeClient(new PayUWebChromeClient(payUCustomBrowser));

                    }

                    @Override
                    public void onBackApprove() {
                        PaymentsActivity.this.finish();
                    }

                    @Override
                    public void onBackDismiss() {
                        super.onBackDismiss();
                    }

                    /**
                     * This callback method will be invoked when setDisableBackButtonDialog is set to true.
                     *
                     * @param alertDialogBuilder a reference of AlertDialog.Builder to customize the dialog
                     */
                    @Override
                    public void onBackButton(AlertDialog.Builder alertDialogBuilder) {
                        super.onBackButton(alertDialogBuilder);
                    }

                    //TODO Below code is used only when magicRetry is set to true in customBrowserConfig
/*                    @Override
                    public void initializeMagicRetry(Bank payUCustomBrowser, WebView webview, MagicRetryFragment magicRetryFragment) {
                        webview.setWebViewClient(new PayUWebViewClient(payUCustomBrowser, magicRetryFragment, merchantKey));
                        Map<String, String> urlList = new HashMap<String, String>();
                        if(payuConfig!=null)
                            urlList.put(url, payuConfig.getData());
                        payUCustomBrowser.setMagicRetry(urlList);
                    }*/
                };

                //Sets the configuration of custom browser
                CustomBrowserConfig customBrowserConfig = new CustomBrowserConfig(merchantKey, txnId);
                customBrowserConfig.setViewPortWideEnable(viewPortWide);

                //TODO don't forgot to set AutoApprove and AutoSelectOTP to true for One Tap payments
                customBrowserConfig.setAutoApprove(false);  // set true to automatically approve the OTP
                customBrowserConfig.setAutoSelectOTP(true); // set true to automatically select the OTP flow

                //Set below flag to true to disable the default alert dialog of Custom Browser and use your custom dialog
                customBrowserConfig.setDisableBackButtonDialog(false);


                //Below flag is used for One Click Payments. It should always be set to CustomBrowserConfig.STOREONECLICKHASH_MODE_SERVER


                //Set it to true to enable run time permission dialog to appear for all Android 6.0 and above devices
                customBrowserConfig.setMerchantSMSPermission(true);

                //Set it to true to enable Magic retry (If MR is enabled SurePay should be disabled and vice-versa)




                //Set it to false if you do not want the transaction with web-collect flow
                //customBrowserConfig.setEnableWebFlow(Payment.TEZ,true);



                /**
                 * Maximum number of times the SurePay dialog box will prompt the user to retry a transaction in case of network failures
                 * Setting the sure pay count to 0, diables the sure pay dialog
                 */
                customBrowserConfig.setEnableSurePay(3);

                //htmlData - HTML string received from PayU webservice using Server to Server call.

               // customBrowserConfig.setHtmlData("");


                //surepayS2Surl - Url on which HTML received from PayU webservice using Server to Server call is hosted.

               // customBrowserConfig.setSurepayS2Surl("");




                /**
                 * set Merchant activity(Absolute path of activity)
                 * By the time CB detects good network, if CBWebview is destroyed, we resume the transaction by passing payment post data to,
                 * this, merchant checkout activity.
                 * */
                customBrowserConfig.setMerchantCheckoutActivityPath("com.payu.testapp.MerchantCheckoutActivity");

                //Set the first url to open in WebView
                customBrowserConfig.setPostURL(url);

                //String postData = "device_type=1&udid=fd7637d3ed7d3ee5&imei=default&key=l80gyM&txnid=MFIT4691279-R1&amount=1&productinfo=gym workout - fitternity test page&firstname=akhil kulkarni&email=akhilkulkarni@fitternity.com&surl=https%3A%2F%2Fwww.fitternity.com%2Fpaymentsuccessandroid&furl=https%3A%2F%2Fwww.fitternity.com%2Fpaymentsuccessandroid&hash=26e07dbe45dee6cfb11c5c4698b3c027a32446d5e7bf84b503566832f452d73775d493b2a8169675aaa17fb8be2e7c2f14c0fb937965f7d86b5e4e5d89db69fc&udf1=&udf2=&udf3=&udf4=&udf5=&phone=7021705378&bankcode=TEZ&pg=upi";

               // String postData = "device_type=1&udid=51e15a3e697d56fe&imei=default&key=smsplus&txnid=1576147630600&amount=10000&productinfo=product_info&firstname=firstname&email=test@gmail.com&surl=+https%3A%2F%2Fpayuresponse.firebaseapp.com%2Fsuccess&furl=https%3A%2F%2Fpayuresponse.firebaseapp.com%2Ffailure&hash=dc48b7ce77bc34744bb0e264b44f302c8c4ec2a0eb171a2d4753eeecff96ce865df0c9e504a46bd72f0718eb56678ed0f5e56f8d3bc4baf1817e8828b88fcf17&udf1=udf1&udf2=udf2&udf3=udf3&udf4=udf4&udf5=udf5&phone=&pg=EMI&bankcode=HDFC03&ccnum=5326760132120978&ccvv=123&ccexpyr=2021&ccexpmon=8&ccname=PayuUser";

               // String postData = "device_type=1&udid=ea569f24d4748199&imei=default&key=l80gyM&txnid=MFIT4582680-R3&amount=300&productinfo=gym workout - zion fitness&firstname=kunal parte&email=kunal_coolrider14@hotmail.com&surl=https%3A%2F%2Fwww.fitternity.com%2Fpaymentsuccessandroid&furl=https%3A%2F%2Fwww.fitternity.com%2Fpaymentsuccessandroid&hash=9baff7f90f7af4d967697bc9495e67c8cb8aaed9ee9ffff6a53a10c0f88c608d10a6ac5d26fc71c554be7e5b6de0d696aec8bd17bdb9b6206553d09018643d4d&udf1=&udf2=&udf3=&udf4=&udf5=&phone=7021705378&bankcode=TEZ&pg=upi";

               // String postData = "device_type=1&udid=51e15a3e697d56fe&imei=default&key=smsplus&txnid=1576214482978&amount=2&productinfo=product_info&firstname=firstname&email=test@gmail.com&surl=+https%3A%2F%2Fpayuresponse.firebaseapp.com%2Fsuccess&furl=https%3A%2F%2Fpayuresponse.firebaseapp.com%2Ffailure&hash=4087690a25942342875eac830da473d0d8ca3a586ec49237816a929f602b5817c9c367474011031e3c1c5e9a441172b77cf90486f86f0a412df72ea4641d5d0e&udf1=udf1&udf2=udf2&udf3=udf3&udf4=udf4&udf5=udf5&phone=&pg=CC&bankcode=CC&ccnum=5326760132120978&ccvv=893&ccexpyr=2021&ccexpmon=7&ccname=PayuUser&si=1&store_card=1&user_credentials=smsplus:ras";

               // String postData = "device_type=1&udid=947b45e0194ca0b6&imei=default&key=VgZldf&txnid=1568978613464&amount=10&productinfo=product_info&firstname=firstname&email=test@gmail.com&surl=+https%3A%2F%2Fpayuresponse.firebaseapp.com%2Fsuccess&furl=https%3A%2F%2Fpayuresponse.firebaseapp.com%2Ffailure&hash=d20b29d2937473616186dc332f9889768e11919252a11347cf5e2cdc67ac1703a475eb4ad2b68349804a71835e55b831516804c7155d7232df799679f924b3f4&udf1=udf1&udf2=udf2&udf3=udf3&udf4=udf4&udf5=udf5&phone=&pg=CC&bankcode=CC&ccnum=5123456789012346&ccvv=123&ccexpyr=2020&ccexpmon=5&ccname=PayuUser&user_credentials=ra:ra&SI=1";

                //  String postData = "device_type=1&udid=ea569f24d4748199&imei=default&key=l80gyM&txnid=MFIT4582680-R3&amount=300&productinfo=gym workout - zion fitness&firstname=kunal parte&email=kunal_coolrider14@hotmail.com&surl=https%3A%2F%2Fwww.fitternity.com%2Fpaymentsuccessandroid&furl=https%3A%2F%2Fwww.fitternity.com%2Fpaymentsuccessandroid&hash=9baff7f90f7af4d967697bc9495e67c8cb8aaed9ee9ffff6a53a10c0f88c608d10a6ac5d26fc71c554be7e5b6de0d696aec8bd17bdb9b6206553d09018643d4d&udf1=&udf2=&udf3=&udf4=&udf5=&phone=7021705378&bankcode=TEZ&pg=upi";
                   // String postData= "device_type=1&udid=51e15a3e697d56fe&imei=default&key=smsplus&txnid=1576147630600&amount=2&productinfo=product_info&firstname=firstname&email=test@gmail.com&surl=+https%3A%2F%2Fpayuresponse.firebaseapp.com%2Fsuccess&furl=https%3A%2F%2Fpayuresponse.firebaseapp.com%2Ffailure&hash=7e4c2be92f650c4c5f192ef6d9ea7a87ea1a435be993b130aa0fabe643e3687dd9da452c5f366e591e9e42be7f418e070df866605d1e309fe0a41d4d2dae7d3c&udf1=udf1&udf2=udf2&udf3=udf3&udf4=udf4&udf5=udf5&phone=&pg=CC&bankcode=CC&ccnum=5326760132120978&ccvv=123&ccexpyr=2021&ccexpmon=8&ccname=PayuUser&store_card=1&user_credentials=sa:sa";
                if (payuConfig!=null)
                customBrowserConfig.setPayuPostData(payuConfig.getData());

               /* if (isPaymentByPhonePe == true & isStandAlonePhonePayAvailable == true) {

                    phonePe.makePayment(payUPhonePeCallback, PaymentsActivity.this, payuConfig.getData(),false);

                } else*/ {

                    new CustomBrowser().addCustomBrowser(PaymentsActivity.this, customBrowserConfig, payUCustomBrowserCallback);
                }


            }
        }
    }
}
