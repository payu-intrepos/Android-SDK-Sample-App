package com.payu.payuui.SdkuiUtil;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.payu.india.Interfaces.BinInfoApiListener;
import com.payu.india.Model.MerchantWebService;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuResponse;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.PostParams.MerchantWebServicePostParams;
import com.payu.india.Tasks.BinInfoTask;
import com.payu.paymentparamhelper.PaymentParams;
import com.payu.paymentparamhelper.siparams.SIParams;

import java.security.MessageDigest;


public class Utils implements BinInfoApiListener {

    private SIParams siParams;
    private Bundle bundle;
    private String salt;
    public Boolean isSiSupported= null;
    public String errorMessage = null;

    public void getBinInfo(Activity activity, PayuConfig payuConfig, PaymentParams mPaymentParams, String cardbin) {
       MerchantWebService merchantWebService = new MerchantWebService();
        siParams = mPaymentParams.getSiParams();
        merchantWebService.setKey(mPaymentParams.getKey());
        merchantWebService.setCommand(PayuConstants.GET_BIN_INFO);
        merchantWebService.setVar1("1");
        if (siParams!=null) merchantWebService.setVar5("1");
        merchantWebService.setVar2(cardbin.replace(" ", ""));
        bundle = activity.getIntent().getExtras();
        salt = bundle.getString(PayuConstants.SALT);

        merchantWebService.setHash(calculateHash("" + mPaymentParams.getKey() + "|" + PayuConstants.GET_BIN_INFO + "|" + 1 + "|" +salt));

       PostData postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();

        if (postData.getCode() == PayuErrors.NO_ERROR) {
            payuConfig.setData(postData.getResult());

            BinInfoTask binInfoTask = new BinInfoTask(this);
            binInfoTask.execute(payuConfig);
        }
        }
    public static String calculateHash(String hashString) {
        try {
            StringBuilder hash = new StringBuilder();
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            messageDigest.update(hashString.getBytes());
            byte[] mdbytes = messageDigest.digest();
            for (byte hashByte : mdbytes) {
                hash.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
            }
            return hash.toString();
        } catch (Exception e) {
            return "ERROR";
        }
    }

    @Override
    public void onBinInfoApiResponse(PayuResponse payuResponse) {
        if ( payuResponse.getCardInformation() != null && siParams!=null) {
            siParams.setCcCardType(getCardType(payuResponse.getCardInformation().getCardType()));
            siParams.setCcCategory(payuResponse.getCardInformation().getCardCategory());
            isSiSupported = payuResponse.getCardInformation().getIsSiSupported();
        }
    }
    private String getCardType(String cardType)  {
        if(cardType.equalsIgnoreCase("CC"))
            return  "CC";
        else return "DC";
    }

}



