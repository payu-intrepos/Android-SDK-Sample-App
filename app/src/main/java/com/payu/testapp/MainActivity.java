package com.payu.testapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.payu.india.Extras.PayUChecksum;
import com.payu.india.Extras.PayUSdkDetails;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Payu.Payu;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.paymentparamhelper.PaymentParams;
import com.payu.paymentparamhelper.PostData;
import com.payu.paymentparamhelper.siparams.BeneficiaryDetails;
import com.payu.paymentparamhelper.siparams.SIParams;
import com.payu.paymentparamhelper.siparams.SIParamsDetails;
import com.payu.paymentparamhelper.siparams.enums.BeneficiaryAccountType;
import com.payu.paymentparamhelper.siparams.enums.BillingCycle;
import com.payu.paymentparamhelper.siparams.enums.BillingLimit;
import com.payu.paymentparamhelper.siparams.enums.BillingRule;
import com.payu.payuui.Activity.PayUBaseActivity;
import com.payu.payuui.SdkuiUtil.SdkUIConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;

/**
 * This activity prepares PaymentParams, fetches hashes from server and send it to PayuBaseActivity.java.
 * <p>
 * Implement this activity with OneClickPaymentListener only if you are integrating One Tap payments.
 */
public class MainActivity extends AppCompatActivity {

    private String merchantKey, userCredentials;

    // These will hold all the payment parameters
    private PaymentParams mPaymentParams;
    private String paymentHash1;

    // This sets the configuration
    private PayuConfig payuConfig;

    private Spinner environmentSpinner;
    private String subventionHash;

    // Used when generating hash from SDK
    private PayUChecksum checksum;
    String salt = null;
    //params for si
    private Boolean isFreeTrial = false;
    private BillingCycle billingCycle = BillingCycle.ONCE;
    private int billingInterval = 1;
    private String billingamount = "1";
    private String billingCurrency = "INR";
    private BillingLimit billingLimit = BillingLimit.ON;
    private BillingRule billingRule = BillingRule.EXACT;
    private String paymentStartDate ="2021-12-24";
    private String paymentEndDate = "2022-12-24";
    private String remarks = " ";
    private String beneficiaryName = "Puspendra";
    private String beneficiaryAccountNumber = "900000000000000";
    private BeneficiaryAccountType beneficiaryAccountType = BeneficiaryAccountType.SAVINGS;

    private Boolean siShow = false;
    private String siHash;
    private Spinner billingCycleSpinner;
    private SwitchCompat freeTrial;
    private EditText billingamountText ;
    private EditText billingIntervalText;
    private EditText paymentStartDateText;
    private EditText paymentEndDateText;
    private SwitchCompat si;
    private LinearLayout siView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO Must write below code in your activity to set up initial context for PayU
        Payu.setInstance(this);
     freeTrial = findViewById(R.id.sp_free_trial);
        // lets set up the tool bar;
        Toolbar toolBar = (Toolbar) findViewById(R.id.app_bar);
        toolBar.setTitle("PayU Demo App");
        toolBar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolBar);

        // lets tell the people what version of sdk we are using
        PayUSdkDetails payUSdkDetails = new PayUSdkDetails();

        Toast.makeText(this, "Build No: " + payUSdkDetails.getSdkBuildNumber() + "\n Build Type: " + payUSdkDetails.getSdkBuildType() + " \n Build Flavor: " + payUSdkDetails.getSdkFlavor() + "\n Application Id: " + payUSdkDetails.getSdkApplicationId() + "\n Version Code: " + payUSdkDetails.getSdkVersionCode() + "\n Version Name: " + payUSdkDetails.getSdkVersionName(), Toast.LENGTH_LONG).show();
        billingCycleSpinner = findViewById(R.id.et_billingCycle_value);
        // Creating adapter for spinner
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, billingCyclearr);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        billingCycleSpinner.setAdapter(arrayAdapter);
      billingCycleSpinner.setSelection(0);
      billingIntervalText = findViewById(R.id.et_billingInterval_value);
      billingamountText = findViewById(R.id.et_billingAmount_value);
      paymentStartDateText = findViewById(R.id.et_paymentStartDate_value);
      paymentEndDateText = findViewById(R.id.et_paymentEndDate_value);
      si = findViewById(R.id.switch_si_on_off);
      siView = findViewById(R.id.siView);
      siView.setVisibility(View.GONE);
        //Lets setup the environment spinner
        environmentSpinner = (Spinner) findViewById(R.id.spinner_environment);
        //  List<String> list = new ArrayList<String>();
        String[] environmentArray = getResources().getStringArray(R.array.environment_array);
/*        list.add("Test");
        list.add("Production");*/
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, environmentArray);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        environmentSpinner.setAdapter(dataAdapter);
        environmentSpinner.setSelection(0);

        environmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (environmentSpinner.getSelectedItem().equals("Production")) {
                    Toast.makeText(MainActivity.this, getString(R.string.use_live_key_in_production_environment), Toast.LENGTH_SHORT).show();

                    /* For test keys, please contact mobile.integration@payu.in with your app name and registered email id
                     */
                    // ((EditText) findViewById(R.id.editTextMerchantKey)).setText("0MQaQP");
                  ((EditText) findViewById(R.id.editTextMerchantKey)).setText("smsplus");
              
                }
                else{
                    //set the test key in test environment
                    ((EditText) findViewById(R.id.editTextMerchantKey)).setText("gtKFFX");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        si.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)siView.setVisibility(View.VISIBLE);
                else siView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            if (data != null) {

                /**
                 * Here, data.getStringExtra("payu_response") ---> Implicit response sent by PayU
                 * data.getStringExtra("result") ---> Response received from merchant's Surl/Furl
                 *
                 * PayU sends the same response to merchant server and in app. In response check the value of key "status"
                 * for identifying status of transaction. There are two possible status like, success or failure
                 * */
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage("Payu's Data : " + data.getStringExtra("payu_response") + "\n\n\n Merchant's Data: " + data.getStringExtra("result"))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }).show();

            } else {
                Toast.makeText(this, getString(R.string.could_not_receive_data), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * This method prepares all the payments params to be sent to PayuBaseActivity.java
     */
    public void navigateToBaseActivity(View view) {


        // merchantKey="";
        merchantKey = ((EditText) findViewById(R.id.editTextMerchantKey)).getText().toString();
        String amount = ((EditText) findViewById(R.id.editTextAmount)).getText().toString();
        String email = ((EditText) findViewById(R.id.editTextEmail)).getText().toString();

        String value = environmentSpinner.getSelectedItem().toString();
        int environment;
        String TEST_ENVIRONMENT = getResources().getString(R.string.test);
        if (value.equals(TEST_ENVIRONMENT))
            environment = PayuConstants.STAGING_ENV;
        else
            environment = PayuConstants.PRODUCTION_ENV;
        isFreeTrial = freeTrial.isChecked();
        userCredentials = merchantKey + ":" + email;
        billingCycle = BillingCycle.valueOf(billingCycleSpinner.getSelectedItem().toString());
        billingInterval = Integer.parseInt(billingIntervalText.getText().toString());
        billingamount = billingamountText.getText().toString();
        paymentStartDate = paymentStartDateText.getText().toString();
        paymentEndDate = paymentEndDateText.getText().toString();
        siShow = si.isChecked();
        //TODO Below are mandatory params for hash genetation
        mPaymentParams = new PaymentParams();
        /**
         * For Test Environment, merchantKey = please contact mobile.integration@payu.in with your app name and registered email id

         */
        mPaymentParams.setKey(merchantKey);
        mPaymentParams.setAmount(amount);
        mPaymentParams.setProductInfo("product_info");
        mPaymentParams.setFirstName("TEST");
        mPaymentParams.setEmail("xyz@gmail.com");
        mPaymentParams.setPhone("");
        if (siShow) {
            mPaymentParams.setSiParams(setSiDeatils());
        }

//        mPaymentParams.setBeneficiaryAccountNumber("50100041412026");

        mPaymentParams.setSubventionAmount(amount);
        mPaymentParams.setSubventionEligibility("all");

        /*
         * Transaction Id should be kept unique for each transaction.
         * */
        mPaymentParams.setTxnId("" + System.currentTimeMillis());
       // mPaymentParams.setTxnId("1587113659761");

        /**
         * Surl --> Success url is where the transaction response is posted by PayU on successful transaction
         * Furl --> Failre url is where the transaction response is posted by PayU on failed transaction
         */
       // mPaymentParams.setSurl(" https://www.fitternity.com/paymentsuccessandroid");
        mPaymentParams.setSurl("https://payuresponse.firebaseapp.com/success");
        mPaymentParams.setFurl("https://payuresponse.firebaseapp.com/failure");
      //  mPaymentParams.setFurl("https://www.fitternity.com/paymentsuccessandroid");
        mPaymentParams.setNotifyURL(mPaymentParams.getSurl());  //for lazy pay

        /*
         * udf1 to udf5 are options params where you can pass additional information related to transaction.
         * If you don't want to use it, then send them as empty string like, udf1=""
         * */
        mPaymentParams.setUdf1("udf1");
        mPaymentParams.setUdf2("udf2");
        mPaymentParams.setUdf3("udf3");
        mPaymentParams.setUdf4("udf4");
        mPaymentParams.setUdf5("udf5");

        /**
         * These are used for store card feature. If you are not using it then user_credentials = "default"
         * user_credentials takes of the form like user_credentials = "merchant_key : user_id"
         * here merchant_key = your merchant key,
         * user_id = unique id related to user like, email, phone number, etc.
         * */
        mPaymentParams.setUserCredentials(userCredentials);

        //TODO Pass this param only if using offer key
       // mPaymentParams.setOfferKey("YONOYSF@6445");

        //TODO Sets the payment environment in PayuConfig object
        payuConfig = new PayuConfig();
        payuConfig.setEnvironment(environment);
        //TODO It is recommended to generate hash from server only. Keep your key and salt in server side hash generation code.
        // generateHashFromServer(mPaymentParams);

        /**
         * Below approach for generating hash is not recommended. However, this approach can be used to test in PRODUCTION_ENV
         * if your server side hash generation code is not completely setup. While going live this approach for hash generation
         * should not be used.
         * */
        if(environment== PayuConstants.STAGING_ENV){
          //  salt = " ";
            salt = "<Please_add_salt_here>";
        }else {
            //Production Env
            salt = "<Please_add_salt_here>";
          
        }
        generateHashFromSDK(mPaymentParams, salt);

    }

    /******************************
     * Client hash generation
     ***********************************/
    // Do not use this, you may use this only for testing.
    // lets generate hashes.
    // This should be done from server side..
    // Do not keep salt anywhere in app.
    public void generateHashFromSDK(PaymentParams mPaymentParams, String salt) {
        PayuHashes payuHashes = new PayuHashes();
        PostData postData = new PostData();

//        if(mPaymentParams.getBeneficiaryAccountNumber()== null){

        // payment Hash;
        checksum = null;
        checksum = new PayUChecksum();
        checksum.setAmount(mPaymentParams.getAmount());
        checksum.setKey(mPaymentParams.getKey());
        checksum.setTxnid(mPaymentParams.getTxnId());
        checksum.setEmail(mPaymentParams.getEmail());
        checksum.setSalt(salt);
        checksum.setProductinfo(mPaymentParams.getProductInfo());
        checksum.setFirstname(mPaymentParams.getFirstName());
        checksum.setUdf1(mPaymentParams.getUdf1());
        checksum.setUdf2(mPaymentParams.getUdf2());
        checksum.setUdf3(mPaymentParams.getUdf3());
        checksum.setUdf4(mPaymentParams.getUdf4());
        checksum.setUdf5(mPaymentParams.getUdf5());

        postData = checksum.getHash();
        if (postData.getCode() == PayuErrors.NO_ERROR) {
            payuHashes.setPaymentHash(postData.getResult());
        }

        if (mPaymentParams.getSubventionAmount() != null && !mPaymentParams.getSubventionAmount().isEmpty()){
            subventionHash = calculateHash(""+mPaymentParams.getKey()+"|"+mPaymentParams.getTxnId()+"|"+mPaymentParams.getAmount()+"|"+mPaymentParams.getProductInfo()+"|"+mPaymentParams.getFirstName()+"|"+mPaymentParams.getEmail()+"|"+mPaymentParams.getUdf1()+"|"+mPaymentParams.getUdf2()+"|"+mPaymentParams.getUdf3()+"|"+mPaymentParams.getUdf4()+"|"+mPaymentParams.getUdf5()+"||||||"+salt+"|"+mPaymentParams.getSubventionAmount());
        }
        if (mPaymentParams.getSiParams()!=null){
            siHash = calculateHash(""+mPaymentParams.getKey()+"|"+mPaymentParams.getTxnId()+"|"+mPaymentParams.getAmount()+"|"+mPaymentParams.getProductInfo()+"|"+mPaymentParams.getFirstName()+"|"+mPaymentParams.getEmail()+"|"+mPaymentParams.getUdf1()+"|"+mPaymentParams.getUdf2()+"|"+mPaymentParams.getUdf3()+"|"+mPaymentParams.getUdf4()+"|"+mPaymentParams.getUdf5()+"||||||"+prepareSiDetails()+"|"+salt);
        }

        /*}

        else {
            String hashString = merchantKey + "|" + mPaymentParams.getTxnId() + "|" + mPaymentParams.getAmount() + "|" + mPaymentParams.getProductInfo() + "|" + mPaymentParams.getFirstName() + "|" + mPaymentParams.getEmail() + "|" + mPaymentParams.getUdf1() + "|" + mPaymentParams.getUdf2() + "|" + mPaymentParams.getUdf3() + "|" + mPaymentParams.getUdf4() + "|" + mPaymentParams.getUdf5() + "||||||{\"beneficiaryAccountNumber\":\"" +mPaymentParams.getBeneficiaryAccountNumber()+ "\"}|" + salt;

            paymentHash1 = calculateHash(hashString);
            payuHashes.setPaymentHash(paymentHash1);



        }*/

        // checksum for payemnt related details
        // var1 should be either user credentials or default
        String var1 = mPaymentParams.getUserCredentials() == null ? PayuConstants.DEFAULT : mPaymentParams.getUserCredentials();
        String key = mPaymentParams.getKey();

        if ((postData = calculateHash(key, PayuConstants.PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK, var1, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR) // Assign post data first then check for success
            payuHashes.setPaymentRelatedDetailsForMobileSdkHash(postData.getResult());
        //vas
        if ((postData = calculateHash(key, PayuConstants.VAS_FOR_MOBILE_SDK, PayuConstants.DEFAULT, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
            payuHashes.setVasForMobileSdkHash(postData.getResult());

        // getIbibocodes
        if ((postData = calculateHash(key, PayuConstants.GET_MERCHANT_IBIBO_CODES, PayuConstants.DEFAULT, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
            payuHashes.setMerchantIbiboCodesHash(postData.getResult());

        if (!var1.contentEquals(PayuConstants.DEFAULT)) {
            // get user card
            if ((postData = calculateHash(key, PayuConstants.GET_USER_CARDS, var1, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR) // todo rename storedc ard
                payuHashes.setStoredCardsHash(postData.getResult());
            // save user card
            if ((postData = calculateHash(key, PayuConstants.SAVE_USER_CARD, var1, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
                payuHashes.setSaveCardHash(postData.getResult());
            // delete user card
            if ((postData = calculateHash(key, PayuConstants.DELETE_USER_CARD, var1, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
                payuHashes.setDeleteCardHash(postData.getResult());
            // edit user card
            if ((postData = calculateHash(key, PayuConstants.EDIT_USER_CARD, var1, salt)) != null && postData.getCode() == PayuErrors.NO_ERROR)
                payuHashes.setEditCardHash(postData.getResult());
        }

        if (mPaymentParams.getOfferKey() != null) {
            postData = calculateHash(key, PayuConstants.OFFER_KEY, mPaymentParams.getOfferKey(), salt);
            if (postData.getCode() == PayuErrors.NO_ERROR) {
                payuHashes.setCheckOfferStatusHash(postData.getResult());
            }
        }

        if (mPaymentParams.getOfferKey() != null && (postData = calculateHash(key, PayuConstants.CHECK_OFFER_STATUS, mPaymentParams.getOfferKey(), salt)) != null && postData.getCode() == PayuErrors.NO_ERROR) {
            payuHashes.setCheckOfferStatusHash(postData.getResult());
        }

        // we have generated all the hases now lest launch sdk's ui
        launchSdkUI(payuHashes);
    }

    // deprecated, should be used only for testing.
    private PostData calculateHash(String key, String command, String var1, String salt) {
        checksum = null;
        checksum = new PayUChecksum();
        checksum.setKey(key);
        checksum.setCommand(command);
        checksum.setVar1(var1);
        checksum.setSalt(salt);
        return checksum.getHash();
    }
    /**
     * This method adds the Payuhashes and other required params to intent and launches the PayuBaseActivity.java
     *
     * @param payuHashes it contains all the hashes generated from merchant server
     */
    public void launchSdkUI(PayuHashes payuHashes) {

        Intent intent = new Intent(this, PayUBaseActivity.class);
        intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
        intent.putExtra(PayuConstants.PAYMENT_PARAMS, mPaymentParams);
        intent.putExtra(SdkUIConstants.SUBVENTION_HASH, subventionHash);
        intent.putExtra(SdkUIConstants.SI_HASH,siHash);
        intent.putExtra(PayuConstants.SALT,salt);
        intent.putExtra(PayuConstants.PAYU_HASHES, payuHashes);
        startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
    }

    /******************************
     * Client hash generation
     ***********************************/
    // Do not use this, you may use this only for testing.
    // lets generate hashes.
    // This should be done from server side..
    // Do not keep salt anywhere in app.
    private String calculateHash(String hashString) {
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

    private SIParams setSiDeatils(){
        SIParams siParams = new SIParams();
        siParams.setFree_trial(false);
    BeneficiaryDetails beneficiaryDetails = new BeneficiaryDetails();
    beneficiaryDetails.setBeneficiaryName(beneficiaryName);
    beneficiaryDetails.setBeneficiaryAccountNumber(beneficiaryAccountNumber);
    beneficiaryDetails.setBeneficiaryAccountType(beneficiaryAccountType);
    siParams.setBeneficiarydetail(beneficiaryDetails);
    SIParamsDetails siParamsDetails = new SIParamsDetails();
    siParamsDetails.setBillingLimit(billingLimit);
    siParamsDetails.setBillingRule(billingRule);
    siParamsDetails.setBillingAmount(billingamount);
    siParamsDetails.setBillingCurrency(billingCurrency);
    siParamsDetails.setBillingCycle(billingCycle);
    siParamsDetails.setBillingInterval(1);
    siParamsDetails.setPaymentStartDate(paymentStartDate);
    siParamsDetails.setPaymentEndDate(paymentEndDate);
    siParamsDetails.setRemarks("");
    siParams.setSi_details(siParamsDetails);
    return siParams;
}
   private JSONObject prepareSiDetails(){
        JSONObject siObject = new JSONObject();
       try {
           siObject.put("billingAmount",billingamount);
           siObject.put("billingCurrency",billingCurrency);
           siObject.put("billingCycle",billingCycle);
           siObject.put("billingInterval",billingInterval);
           siObject.put("paymentStartDate",paymentStartDate);
           siObject.put("paymentEndDate",paymentEndDate);
           siObject.put("billingLimit",billingLimit);
           siObject.put("billingRule",billingRule);
       } catch (JSONException e) {
           e.printStackTrace();
       }
       return siObject;
   }

   String[] billingCyclearr= {"DAILY", "WEEKLY", "MONTHLY", "YEARLY", "ONCE", "ADHOC"};

 private void hidesiview(){

 }
}
