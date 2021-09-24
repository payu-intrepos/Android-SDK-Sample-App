package com.payu.payuui.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.payu.india.Interfaces.CheckoutDetailsListener;
import com.payu.india.Model.CheckoutFilter;
import com.payu.india.Model.CustomerDetails;
import com.payu.india.Model.Emi;
import com.payu.india.Model.GetCheckoutDetailsRequest;
import com.payu.india.Model.GetTransactionDetails;
import com.payu.india.Model.MerchantWebService;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuResponse;
import com.payu.india.Model.PostData;
import com.payu.india.Model.Usecase;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.PostParams.MerchantWebServicePostParams;
import com.payu.india.Tasks.GetCheckoutDetailsTask;
import com.payu.paymentparamhelper.PaymentParams;
import com.payu.payuui.R;
import com.payu.payuui.SdkuiUtil.SdkUIConstants;
import com.payu.payuui.SdkuiUtil.SdkuiUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class ZestmoneyFragment extends Fragment implements View.OnClickListener, CheckoutDetailsListener {

    private EditText etMobileNumberZestmoney;
    private PaymentParams mPaymentParams;
    private TextView mTvEligibilityMessage;
    private String salt;
    private ProgressDialog progressDialog;
    private String errorMessage = "Not Eligible for Zestmoney";
    private PayuConfig payuConfig = null;

    public ZestmoneyFragment() {
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_zestmoney, container, false);
        setUpUI(view);
        mPaymentParams = getArguments().getParcelable(PayuConstants.KEY);
        payuConfig = getArguments().getParcelable(PayuConstants.PAYU_CONFIG);
        salt = getArguments().getString(PayuConstants.SALT);
        return view;

    }

    /**
     * Initialize UI Component of fragment
     *
     * @param view fragment layout
     */
    private void setUpUI(View view) {
        etMobileNumberZestmoney = view.findViewById(R.id.etZestmoneyMobileNumber);
        Button mBtnCheckEligibility = view.findViewById(R.id.btnCheckElibility);
        mTvEligibilityMessage = view.findViewById(R.id.tvEligibilityMessage);
        mBtnCheckEligibility.setOnClickListener(this);
    }

    private void showProgressDialog(){
        progressDialog = new ProgressDialog(this.getActivity());
        progressDialog.setMessage("Checking Eligibility...");
        progressDialog.show();
    }

    private void hideProgressDialog(){
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    /**
     * Called when activity staretd for result is finished
     *
     * @param requestCode return code from activity
     * @param resultCode  result from activity
     * @param data        intent from stated activity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            getActivity().setResult(resultCode, data);
            getActivity().finish();
        }
    }

    private void checkEligibility(String mobileNumber) {
        Usecase usecase = new Usecase.Builder().setCheckCustomerEligibility(true).shouldGetExtendedPaymentDetails(true).build();
        GetTransactionDetails transactionDetails = new GetTransactionDetails.Builder().setAmount(Double.parseDouble(mPaymentParams.getAmount())).build();
        CustomerDetails customerDetails = new CustomerDetails.Builder().setMobile(mobileNumber).build();
        CheckoutFilter checkoutFilter = new CheckoutFilter.Builder()
                .setPaymentOptionName(SdkUIConstants.EMI.toLowerCase())
                .setPaymentOptionValue(SdkUIConstants.ZESTMON)
                .setPaymentOptionType(SdkUIConstants.CARDLESS).build();
        String var1 = new GetCheckoutDetailsRequest.Builder()
                .setUsecase(usecase)
                .setCustomerDetails(customerDetails)
                .setCheckoutFilter(checkoutFilter)
                .setTransactionDetails(transactionDetails)
                .build().prepareJSON();

        MerchantWebService merchantWebService = new MerchantWebService();
        merchantWebService.setKey(mPaymentParams.getKey());
        merchantWebService.setCommand(PayuConstants.GET_CHECKOUT_DETAILS);
        merchantWebService.setVar1(var1);
        merchantWebService.setHash(encryptThisString(mPaymentParams.getKey()+"|"+PayuConstants.GET_CHECKOUT_DETAILS+"|"+var1+"|"+salt));

        PostData postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();
        if (postData.getCode() == PayuErrors.NO_ERROR){
            if (payuConfig == null)
                payuConfig = new PayuConfig();
            payuConfig.setData(postData.getResult());
            showProgressDialog();
            GetCheckoutDetailsTask getCheckoutDetailsTask = new GetCheckoutDetailsTask(this);
            getCheckoutDetailsTask.execute(payuConfig);
        }
    }

    public static String encryptThisString(String input) {
        try {
            // getInstance() method is called with algorithm SHA-512
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnCheckElibility) {
            SdkuiUtils.hideSoftKeyboard(getActivity());
            if (etMobileNumberZestmoney != null && etMobileNumberZestmoney.getText().toString().trim().length() > 0) {
                mTvEligibilityMessage.setText("");
                checkEligibility(etMobileNumberZestmoney.getText().toString().trim());
            } else {
                Toast.makeText(getActivity(), "Please provide a mobile number.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (view.getId()== R.id.button_pay_now){
            Toast.makeText(getActivity(), "Btn pressed!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isZestmoneyEligible(PayuResponse payuResponse){
        if (payuResponse.isCardLessEmiAvailable()){
            ArrayList<Emi> cardlessEmi = payuResponse.getCardlessemi();
            for (Emi emi: cardlessEmi){
                if (emi.getBankName().equalsIgnoreCase(SdkUIConstants.ZESTMON))
                    if (emi.getStatus())
                        return true;
                    else
                        errorMessage = emi.getReason();
            }
        }
        return false;
    }

    private void showEligibilityMessage(String message){
        mTvEligibilityMessage.setText(message);
        mTvEligibilityMessage.setVisibility(View.VISIBLE);
    }

    private void showErrorView(){
        if (getActivity() != null)
            getActivity().findViewById(R.id.button_pay_now).setEnabled(false);
        showEligibilityMessage(errorMessage);
    }

    @Override
    public void onCheckoutDetailsResponse(PayuResponse payuResponse) {
        hideProgressDialog();
        if (payuResponse != null && payuResponse.getResponseStatus() != null && payuResponse.getResponseStatus().getStatus().equalsIgnoreCase(PayuConstants.SUCCESS)){
            boolean isEligible = isZestmoneyEligible(payuResponse);
            if (isEligible) {
                if (getActivity() != null)
                    getActivity().findViewById(R.id.button_pay_now).setEnabled(true);
                showEligibilityMessage("Eligible for Zestmoney");
            }else
                showErrorView();

        } else
            showErrorView();
    }
}