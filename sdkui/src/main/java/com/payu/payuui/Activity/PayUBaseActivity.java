package com.payu.payuui.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.payu.custombrowser.CustomBrowser;
import com.payu.custombrowser.PayUCustomBrowserCallback;
import com.payu.custombrowser.bean.CustomBrowserResultData;
import com.payu.custombrowser.util.PaymentOption;
import com.payu.india.Interfaces.BinInfoApiListener;
import com.payu.india.Interfaces.PaymentRelatedDetailsListener;
import com.payu.india.Interfaces.ValueAddedServiceApiListener;
import com.payu.india.Model.Emi;
import com.payu.india.Model.MerchantWebService;
import com.payu.india.Model.PaymentDetails;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PayuResponse;
import com.payu.india.Model.StoredCard;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.Payu.PayuUtils;
import com.payu.india.PostParams.MerchantWebServicePostParams;
import com.payu.india.Tasks.BinInfoTask;
import com.payu.india.Tasks.GetPaymentRelatedDetailsTask;
import com.payu.india.Tasks.ValueAddedServiceTask;
import com.payu.paymentparamhelper.PaymentParams;
import com.payu.paymentparamhelper.PaymentPostParams;
import com.payu.paymentparamhelper.PostData;
import com.payu.paymentparamhelper.siparams.SIParams;
import com.payu.payuui.Adapter.PagerAdapter;
import com.payu.payuui.Adapter.SavedCardItemFragmentAdapter;
import com.payu.payuui.Fragment.CreditDebitFragment;
import com.payu.payuui.Fragment.SavedCardItemFragment;
import com.payu.payuui.R;
import com.payu.payuui.SdkuiUtil.SdkUIConstants;
import com.payu.payuui.Widget.SwipeTab.SlidingTabLayout;
import com.payu.samsungpay.PayUSamsungPay;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This activity is where you get the payment options.
 */
public class PayUBaseActivity extends FragmentActivity implements PaymentRelatedDetailsListener, ValueAddedServiceApiListener,BinInfoApiListener, View.OnClickListener {

    public Bundle bundle;
    private ArrayList<String> paymentOptionsList = new ArrayList<String>();
    private PayuConfig payuConfig;
    private PaymentParams mPaymentParams;
    private PaymentPostParams paymentPostParams;
    private String merchantKey;
    private String userCredentials;
    private PayuHashes mPayUHashes;
    public PayuResponse mPayuResponse;
    private PayuUtils mPayuUtils;
    private PayuResponse valueAddedResponse;
    private PayuResponse binInfoResponse;
    private PagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private SlidingTabLayout slidingTabLayout;
    private Button payNowButton;
    private PaymentOption paymentOption;
    private Spinner spinnerNetbanking;
    private String bankCode;
    private PostData postData;
    private String samPayPostData;
    private ValueAddedServiceTask valueAddedServiceTask;
    private ArrayList<StoredCard> savedCards;

    private PostData mPostData;
    private PayUSamsungPay payUSamsungPay;
    private ProgressBar mProgressBar;
    private boolean isSamsungPaySupported = false;
    private boolean isPhonePeSupported = false;
    public String cardbin;

    private SIParams siParams;
    private String salt;
    public Boolean isSiSupported= null;
    public String errorMessage = null;
   // private  boolean isPaymentByPhonePe = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payu_base);

        (payNowButton = (Button) findViewById(R.id.button_pay_now)).setOnClickListener(this);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        bundle = getIntent().getExtras();

        if (bundle != null) {
            payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);
            payuConfig = null != payuConfig ? payuConfig : new PayuConfig();

            mPayuUtils = new PayuUtils();

            mPaymentParams = bundle.getParcelable(PayuConstants.PAYMENT_PARAMS);
            mPayUHashes = bundle.getParcelable(PayuConstants.PAYU_HASHES);
            salt = bundle.getString(PayuConstants.SALT);
            merchantKey = mPaymentParams.getKey();
            userCredentials = mPaymentParams.getUserCredentials();
            siParams = mPaymentParams.getSiParams();


         // Call back method of PayU custom browser to check availability of Samsung Pay

            PayUCustomBrowserCallback payUCustomBrowserCallback = new PayUCustomBrowserCallback() {

                @Override
                public void onCBErrorReceived(int code, String errormsg) {
                    super.onCBErrorReceived(code, errormsg);
                }

                @Override
                public void isPaymentOptionAvailable(CustomBrowserResultData resultData) {
                    switch (resultData.getPaymentOption()) {
                        case SAMSUNGPAY:
                            isSamsungPaySupported = resultData.isPaymentOptionAvailable();
                            break;
                        case PHONEPE:
                            isPhonePeSupported = resultData.isPaymentOptionAvailable();
                            break;

                    }
                }

            };

            //In this method we check the availability of Samsung Pay as Payment option on device being used

            new CustomBrowser().checkForPaymentAvailability(this, paymentOption.SAMSUNGPAY, payUCustomBrowserCallback, mPayUHashes.getPaymentRelatedDetailsForMobileSdkHash(), merchantKey, userCredentials);
            new CustomBrowser().checkForPaymentAvailability(this, paymentOption.PHONEPE, payUCustomBrowserCallback, mPayUHashes.getPaymentRelatedDetailsForMobileSdkHash(), merchantKey, userCredentials);

            ((TextView) findViewById(R.id.textview_amount)).setText(SdkUIConstants.AMOUNT + ": " + mPaymentParams.getAmount());
            ((TextView) findViewById(R.id.textview_txnid)).setText(SdkUIConstants.TXN_ID + ": " + mPaymentParams.getTxnId());

            if (mPaymentParams != null && mPayUHashes != null && payuConfig != null) {
                /**
                 * Below merchant webservice is used to get all the payment options enabled on the merchant key.
                 */
                MerchantWebService merchantWebService = new MerchantWebService();
                merchantWebService.setKey(mPaymentParams.getKey());
                merchantWebService.setCommand(PayuConstants.PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK);
                merchantWebService.setVar1(mPaymentParams.getUserCredentials() == null ? "default" : mPaymentParams.getUserCredentials());

                if (mPaymentParams.getSubventionEligibility() != null && !mPaymentParams.getSubventionEligibility().isEmpty())
                    merchantWebService.setVar2(mPaymentParams.getSubventionEligibility());
                merchantWebService.setHash(mPayUHashes.getPaymentRelatedDetailsForMobileSdkHash());

                // fetching for the first time.
                if (null == savedInstanceState) { // dont fetch the data if its been called from payment activity.
                    PostData postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();
                    if (postData.getCode() == PayuErrors.NO_ERROR) {
                        // ok we got the post params, let make an api call to payu to fetch the payment related details
                        payuConfig.setData(postData.getResult());

                        // lets set the visibility of progress bar
                        mProgressBar.setVisibility(View.VISIBLE);

                        GetPaymentRelatedDetailsTask paymentRelatedDetailsForMobileSdkTask = new GetPaymentRelatedDetailsTask(this);
                        paymentRelatedDetailsForMobileSdkTask.execute(payuConfig);
                    } else {
                        Toast.makeText(this, postData.getResult(), Toast.LENGTH_LONG).show();
//                 close the progress bar
                        mProgressBar.setVisibility(View.GONE);
                    }
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.could_not_receive_data), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onValueAddedServiceApiResponse(PayuResponse payuResponse) {
        valueAddedResponse = payuResponse;

        if (mPayuResponse != null) {
            if (mPayuResponse.isCreditCardAvailable() && mPayuResponse.isDebitCardAvailable()) {
                //Disable the pay button initially for CC/DC
                payNowButton.setEnabled(false);
            } else {
                //Enable the pay button for all other options
                payNowButton.setEnabled(true);
            }
            setupViewPagerAdapter(mPayuResponse, valueAddedResponse,binInfoResponse);
        }
    }

    @Override
    public void onPaymentRelatedDetailsResponse(PayuResponse payuResponse) {
        mPayuResponse = payuResponse;


        boolean lazypay = mPayuResponse.isLazyPayAvailable();

        if (valueAddedResponse != null)
            setupViewPagerAdapter(mPayuResponse, valueAddedResponse,binInfoResponse);

        MerchantWebService valueAddedWebService = new MerchantWebService();
        valueAddedWebService.setKey(mPaymentParams.getKey());
        valueAddedWebService.setCommand(PayuConstants.VAS_FOR_MOBILE_SDK);
        valueAddedWebService.setHash(mPayUHashes.getVasForMobileSdkHash());
        valueAddedWebService.setVar1(PayuConstants.DEFAULT);
        valueAddedWebService.setVar2(PayuConstants.DEFAULT);
        valueAddedWebService.setVar3(PayuConstants.DEFAULT);

        if ((postData = new MerchantWebServicePostParams(valueAddedWebService).getMerchantWebServicePostParams()) != null && postData.getCode() == PayuErrors.NO_ERROR) {
            payuConfig.setData(postData.getResult());
            valueAddedServiceTask = new ValueAddedServiceTask(this);
            valueAddedServiceTask.execute(payuConfig);
        } else {
            if (postData != null)
                Toast.makeText(this, postData.getResult(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * This method sets us the view pager with payment options.
     *
     * @param payuResponse       contains the payment options available on the merchant key
     * @param valueAddedResponse contains the bank down status for various banks
     */
    private void setupViewPagerAdapter(final PayuResponse payuResponse, PayuResponse valueAddedResponse,PayuResponse binInfoResponse) {

        if (payuResponse.isResponseAvailable() && payuResponse.getResponseStatus().getCode() == PayuErrors.NO_ERROR) { // ok we are good to go
            Toast.makeText(this, payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();


            if (payuResponse.isStoredCardsAvailable()) {
                paymentOptionsList.add(SdkUIConstants.SAVED_CARDS);

            }

            if (payuResponse.isCreditCardAvailable() || payuResponse.isDebitCardAvailable()) {
                paymentOptionsList.add(SdkUIConstants.CREDIT_DEBIT_CARDS);
            }

            if (payuResponse.isNetBanksAvailable()) { // okay we have net banks now.
                paymentOptionsList.add(SdkUIConstants.NET_BANKING);
            }

/*            if(payuResponse.isEmiAvailable()){
                paymentOptionsList.add(SdkUIConstants.EMI);
            }*/

            if(payuResponse.isCashCardAvailable()){
                paymentOptionsList.add(SdkUIConstants.CASH_CARDS);
            }

            if (payuResponse.isUpiAvailable()) { // adding UPI
                paymentOptionsList.add(SdkUIConstants.UPI);
            }

            if (payuResponse.isGoogleTezAvailable()) { // adding TEZ
                paymentOptionsList.add(SdkUIConstants.TEZ);

            }

            if(payuResponse.isGenericIntentAvailable()){
                paymentOptionsList.add(SdkUIConstants.GENERICINTENT);
            }


            if (payuResponse.isPaisaWalletAvailable() && payuResponse.getPaisaWallet().get(0).getBankCode().contains(PayuConstants.PAYUW)) {
                paymentOptionsList.add(SdkUIConstants.PAYU_MONEY);
            }

            if (payuResponse.isLazyPayAvailable()) {
                paymentOptionsList.add(SdkUIConstants.LAZY_PAY); // added Lazy Pay Option

            }
            if (isSamsungPaySupported==true) {
                paymentOptionsList.add("SAMPAY");
            }

           /* if(payuResponse.isPhonePeIntentAvailable()){

                paymentOptionsList.add(SdkUIConstants.PHONEPE);
            }*/
            if (isPhonePeSupported) {
                paymentOptionsList.add(SdkUIConstants.PHONEPE);
            }

            paymentOptionsList.add(SdkUIConstants.ZESTMONEY);
        }

            else {
            Toast.makeText(this, "Something went wrong : " + payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
        }

        //DO NOT KEEP SALT ON CLIENT SIDE. Always keep it on Server Side
        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), paymentOptionsList, payuResponse, valueAddedResponse,salt);
        pagerAdapter.setPayuConfig(payuConfig);
        pagerAdapter.setPaymentParams(mPaymentParams);

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tab_layout);
        slidingTabLayout.setDistributeEvenly(false);

        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        slidingTabLayout.setViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                switch (paymentOptionsList.get(position)) {
                    case SdkUIConstants.SAVED_CARDS:
                        ViewPager myViewPager = (ViewPager) findViewById(R.id.pager_saved_card);
                        int currentPosition = ((ViewPager) findViewById(R.id.pager_saved_card)).getCurrentItem();
                        savedCards = payuResponse != null ? payuResponse.getStoredCards() : null;
                        if (savedCards != null) {
                            if (savedCards.size() == 0) {
                                payNowButton.setEnabled(false);
                                break;
                            }

                           //   cardbin=  savedCards.get(currentPosition).getCardBin();
                          //  utils.getBinInfo(PayUBaseActivity.this,payuConfig,mPaymentParams,cardbin);
                                if (savedCards.get(currentPosition).getCardType().equals("SMAE")) {
                                payNowButton.setEnabled(true);
                            } else {
                                SavedCardItemFragmentAdapter mSaveAdapter = (SavedCardItemFragmentAdapter) myViewPager.getAdapter();
                                SavedCardItemFragment mSaveFragment = mSaveAdapter.getFragment(currentPosition) instanceof SavedCardItemFragment ? mSaveAdapter.getFragment(currentPosition) : null;

                                if (mSaveFragment != null && mSaveFragment.cvvValidation()) {
                                    payNowButton.setEnabled(true);
                                } else {
                                    payNowButton.setEnabled(false);
                                }
                            }
                        }
                        break;
                    case SdkUIConstants.CREDIT_DEBIT_CARDS:
                        PagerAdapter mPagerAdapter = (PagerAdapter) viewPager.getAdapter();
                        CreditDebitFragment tempCreditDebitFragment = mPagerAdapter.getFragment(position) instanceof CreditDebitFragment ? (CreditDebitFragment) mPagerAdapter.getFragment(position) : null;
                        if (tempCreditDebitFragment != null)
                            tempCreditDebitFragment.checkData();
                        break;
                    case SdkUIConstants.NET_BANKING:
                    case SdkUIConstants.PAYU_MONEY:
                    case SdkUIConstants.UPI:
                    case SdkUIConstants.TEZ:
                    case SdkUIConstants.GENERICINTENT:
                    case SdkUIConstants.LAZY_PAY:
                    case "SAMPAY":
                    case SdkUIConstants.PHONEPE:
                    case SdkUIConstants.EMI:
                    case SdkUIConstants.CASH_CARDS:
                        payNowButton.setEnabled(true);
                        hideKeyboard();
                        break;
                    case SdkUIConstants.ZESTMONEY:
                        payNowButton.setEnabled(false);
                        hide_keyboard();
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + paymentOptionsList.get(position));

                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mProgressBar.setVisibility(View.GONE);

    }

    public void hide_keyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = this.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if(view == null) {
            view = new View(this);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_pay_now) {

            mPostData = null;

            if (mPayUHashes != null)
                mPaymentParams.setHash(mPayUHashes.getPaymentHash());

            if (paymentOptionsList != null && paymentOptionsList.size() > 0) {
                switch (paymentOptionsList.get(viewPager.getCurrentItem())) {
                    case SdkUIConstants.SAVED_CARDS:
                   //     setBinInfoResponse();
                        makePaymentByStoredCard();
                        break;
                    case SdkUIConstants.CREDIT_DEBIT_CARDS:
                        makePaymentByCreditCard();
                        break;
                    case SdkUIConstants.NET_BANKING:
                        makePaymentByNB();
                        break;
                    case SdkUIConstants.CASH_CARDS:
                        makePaymentByWallets();
                        break;
                    case SdkUIConstants.EMI:
                        makePaymentByEMI();
                        break;
                    case SdkUIConstants.PAYU_MONEY:
                        makePaymentByPayUMoney();
                        break;
                    case SdkUIConstants.UPI:
                        makePaymentByUPI();
                        break;
                    case SdkUIConstants.TEZ:
                        makePaymentByTEZ();
                        break;
                    case SdkUIConstants.GENERICINTENT:
                        makePaymentByGenericIntent();
                        break;

                    case SdkUIConstants.LAZY_PAY:
                        makePaymentByLazyPay();
                        break;
                    case "SAMPAY":
                        makePaymentBySamPay();
                        break;
                   /* case SdkUIConstants.PHONEPE:
                        isPaymentByPhonePe= true;
                        makePaymentByPhonePe();
                        break;*/

                    case SdkUIConstants.PHONEPE:
                        makePaymentByPhonePe();
                        break;
                    case SdkUIConstants.ZESTMONEY:
                        makePaymentByZestmoney();
                        break;
                }
            }

            if (mPostData != null) {
                if (mPostData.getCode() == PayuErrors.NO_ERROR) {
                    payuConfig.setData(mPostData.getResult());
                    Intent intent = new Intent(this, PaymentsActivity.class);
                    intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
                    startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
                } else {
                    Toast.makeText(this, mPostData.getResult(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }





    private void makePaymentByPayUMoney() {

        try {
            mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.PAYU_MONEY).getPaymentPostParams();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makePaymentByTEZ(){
        try {
            mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.TEZ).getPaymentPostParams();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makePaymentByGenericIntent(){
        try {
            mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.UPI_INTENT).getPaymentPostParams();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makePaymentByPhonePe(){

        try {
            mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.PHONEPE_INTENT).getPaymentPostParams();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void makePaymentByZestmoney() {
        try {
            EditText etMobileNumberZestmoney = findViewById(R.id.etZestmoneyMobileNumber);
            if (etMobileNumberZestmoney != null) {
                mPaymentParams.setPhone(etMobileNumberZestmoney.getText().toString().trim());
                mPaymentParams.setBankCode(SdkUIConstants.ZESTMON);
                mPaymentParams.setCardlessEMI(true);
                try {
                    mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.EMI).getPaymentPostParams();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void makePaymentByLazyPay(){

        try{

            mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.LAZYPAY).getPaymentPostParams();

        }
        catch (Exception e){

      Log.e("error",e+"");

        }

    }
    private void makePaymentBySamPay(){

        try{

            mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.SAMSUNG_PAY).getPaymentPostParams();

        }
        catch (Exception e){

      Log.e("error",e+"");

        }

    }

    private void makePaymentByCreditCard() {
        CheckBox saveCardCheckBox = (CheckBox) findViewById(R.id.check_box_save_card);
        CheckBox enableOneClickPaymentCheckBox = (CheckBox) findViewById(R.id.check_box_enable_oneclick_payment);

        if (saveCardCheckBox.isChecked()) {
            mPaymentParams.setStoreCard(1);
        } else {
            mPaymentParams.setStoreCard(0);
        }




        // lets try to get the post params
        mPaymentParams.setCardNumber(((EditText) findViewById(R.id.edit_text_card_number)).getText().toString().replace(" ", ""));
        mPaymentParams.setNameOnCard(((EditText) findViewById(R.id.edit_text_name_on_card)).getText().toString());
        mPaymentParams.setExpiryMonth(((EditText) findViewById(R.id.edit_text_expiry_month)).getText().toString());
        mPaymentParams.setExpiryYear(((EditText) findViewById(R.id.edit_text_expiry_year)).getText().toString());
        mPaymentParams.setCvv(((EditText) findViewById(R.id.edit_text_card_cvv)).getText().toString());
        if (mPaymentParams.getSiParams()!=null){
            String siHash = bundle.getString(SdkUIConstants.SI_HASH);
            if (siHash!=null && siHash.isEmpty()==false){
                mPaymentParams.setHash(siHash);
            }
        }
        if (mPaymentParams.getStoreCard() == 1 && !((EditText) findViewById(R.id.edit_text_card_label)).getText().toString().isEmpty())
            mPaymentParams.setCardName(((EditText) findViewById(R.id.edit_text_card_label)).getText().toString());
        else if (mPaymentParams.getStoreCard() == 1 && ((EditText) findViewById(R.id.edit_text_card_label)).getText().toString().isEmpty())
            mPaymentParams.setCardName(((EditText) findViewById(R.id.edit_text_name_on_card)).getText().toString());

        try {
            mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.CC).getPaymentPostParams();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void makePaymentByNB() {

        spinnerNetbanking = (Spinner) findViewById(R.id.spinner);
        ArrayList<PaymentDetails> netBankingList = null;
        if(mPayuResponse!=null && (mPaymentParams.getSiParams()==null || mPaymentParams.getSiParams().toString().isEmpty()) )
        netBankingList = mPayuResponse.getNetBanks();
        else
        netBankingList = mPayuResponse.getSiBankList();
        if(netBankingList!=null && netBankingList.get(spinnerNetbanking.getSelectedItemPosition()) !=null)
        bankCode = netBankingList.get(spinnerNetbanking.getSelectedItemPosition()).getBankCode();
        if (mPaymentParams.getSiParams()!=null){
            String siHash = bundle.getString(SdkUIConstants.SI_HASH);
            if (siHash!=null && siHash.isEmpty()==false){
                mPaymentParams.setHash(siHash);
            }
        }
        mPaymentParams.setBankCode(bankCode);

        try {
            mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.NB).getPaymentPostParams();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void makePaymentByWallets() {

        spinnerNetbanking = (Spinner) findViewById(R.id.spinnerWallets);
        ArrayList<PaymentDetails> walletList = null;
        if(mPayuResponse!=null)
            walletList = mPayuResponse.getCashCard();

        if(walletList!=null && walletList.get(spinnerNetbanking.getSelectedItemPosition()) !=null)
            bankCode = walletList.get(spinnerNetbanking.getSelectedItemPosition()).getBankCode();

        mPaymentParams.setBankCode(bankCode);

        try {
            mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.CASH).getPaymentPostParams();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void makePaymentByEMI(){

        Spinner emiDurationSpinner = (Spinner) findViewById(R.id.spinner_emi_duration);
        EditText cardNumberEditText = (EditText) findViewById(R.id.edit_text_emi_card_number);
        EditText nameOnCardEditText = (EditText) findViewById(R.id.edit_text_emi_name_on_card);
        EditText cvvEditText = (EditText) findViewById(R.id.edit_text_emi_cvv);
        EditText expiryMonthEditText = (EditText) findViewById(R.id.edit_text_emi_expiry_month);
        EditText expiryYearEditText = (EditText) findViewById(R.id.edit_text_emi_expiry_year);

        Emi selectedEmi = (Emi) emiDurationSpinner.getSelectedItem();
        bankCode = selectedEmi.getBankCode();

        mPaymentParams.setCardNumber(cardNumberEditText.getText().toString());
        mPaymentParams.setNameOnCard(nameOnCardEditText.getText().toString());
        mPaymentParams.setExpiryMonth(expiryMonthEditText.getText().toString());
        mPaymentParams.setExpiryYear(expiryYearEditText.getText().toString());
        mPaymentParams.setCvv(cvvEditText.getText().toString());
        mPaymentParams.setBankCode(bankCode);

        if (mPaymentParams.getSubventionAmount() != null && !mPaymentParams.getSubventionAmount().isEmpty()){
            String subventionHash = bundle.getString(SdkUIConstants.SUBVENTION_HASH);
            if (subventionHash != null && !subventionHash.isEmpty()) {
                mPaymentParams.setHash(subventionHash);
            }
        }

        try {
            mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.EMI).getPaymentPostParams();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void makePaymentByStoredCard() {

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager_saved_card);
        StoredCard selectedStoredCard = mPayuResponse.getStoredCards().get(viewPager.getCurrentItem());
        SavedCardItemFragmentAdapter mSaveAdapter = (SavedCardItemFragmentAdapter) viewPager.getAdapter();
        SavedCardItemFragment mSaveFragment = mSaveAdapter.getFragment(viewPager.getCurrentItem()) instanceof SavedCardItemFragment ? mSaveAdapter.getFragment(viewPager.getCurrentItem()) : null;
        String cvv = mSaveFragment !=null ? mSaveFragment.getCvv() : null;
        // lets try to get the post params
        selectedStoredCard.setCvv(cvv); // make sure that you set the cvv also


        mPaymentParams.setCardToken(selectedStoredCard.getCardToken());
        mPaymentParams.setNameOnCard(selectedStoredCard.getNameOnCard());
        mPaymentParams.setCardName(selectedStoredCard.getCardName());
        mPaymentParams.setExpiryMonth(selectedStoredCard.getExpiryMonth());
        mPaymentParams.setExpiryYear(selectedStoredCard.getExpiryYear());
        mPaymentParams.setCvv(cvv);
        if (mPaymentParams.getSiParams()!=null){
            String siHash = bundle.getString(SdkUIConstants.SI_HASH);
            if (siHash!=null && siHash.isEmpty()==false){
                mPaymentParams.setHash(siHash);
            }
        }

              try {
                  mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.CC).getPaymentPostParams();
              } catch (Exception e) {
                  e.printStackTrace();
              }
    }

//    private void makePaymentBySamPay() {
//        // Here we are generating the Post Data for payment through samsung pay
//         samPayPostData ="txnid=" + mPaymentParams.getTxnId() +
//                 "&productinfo=" + mPaymentParams.getProductInfo() +
//                 "&user_credentials=" + userCredentials +
//                 "&key=" + mPaymentParams.getKey() +
//                 "&surl="+ mPaymentParams.getSurl()+
//                 "&furl=" + mPaymentParams.getFurl() +
//                 "&firstname=" + mPaymentParams.getFirstName() +
//                 "&email=" + mPaymentParams.getEmail() +
//                 "&amount=" +mPaymentParams.getAmount() +
//                 "&udf1=" + mPaymentParams.getUdf1() +
//                 "&udf2=" + mPaymentParams.getUdf2() +
//                 "&udf3=" + mPaymentParams.getUdf3() +
//                 "&udf4=" + mPaymentParams.getUdf4() +
//                 "&udf5=" + mPaymentParams.getUdf5() +
//                 "&pg=" + "SAMPAY" +
//                 "&bankcode=" + "SAMPAY" +
//                 "&hash=" + mPayUHashes.getPaymentHash();
//    }

    /**
     * Validate VPA and Calculate post data
     */
    private void makePaymentByUPI() {

        EditText etVirtualAddress = (EditText) findViewById(R.id.et_virtual_address);
        // Virtual address Check (vpa check)
        // 1)Vpa length should be less than or equal to 50
        // 2)It can be alphanumeric and can contain a dot(.).
        // 3)It should contain a @
        if(etVirtualAddress.getText()!=null && etVirtualAddress.getText().toString().trim().length()==0){
            etVirtualAddress.requestFocus();
            etVirtualAddress.setError(getBaseContext().getText(R.string.error_fill_vpa));

        }else {
            if(etVirtualAddress.getText().toString().trim().length()> PayuConstants.MAX_VPA_SIZE){
                etVirtualAddress.setError(getBaseContext().getText(R.string.error_invalid_vpa));
            }else if(!etVirtualAddress.getText().toString().trim().contains("@")){
                etVirtualAddress.setError(getBaseContext().getText(R.string.error_invalid_vpa));
            }else{
                String userVirtualAddress= etVirtualAddress.getText().toString().trim();
                Pattern pattern = Pattern.compile("^([A-Za-z0-9\\.])+\\@[A-Za-z0-9]+$");
                Matcher matcher = pattern.matcher(userVirtualAddress);
                if (matcher.matches()) {
                    mPaymentParams.setVpa(userVirtualAddress);
                    try {
                        mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.UPI).getPaymentPostParams();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else {
                    etVirtualAddress.setError(getBaseContext().getText(R.string.error_invalid_vpa));
                }


            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            //Lets pass the result back to previous activity
            setResult(resultCode, data);
            finish();
        }
    }

    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = this.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void getBinInfo(String cardbin) {
        MerchantWebService merchantWebService = new MerchantWebService();
        merchantWebService.setKey(mPaymentParams.getKey());
        merchantWebService.setCommand(PayuConstants.GET_BIN_INFO);
        merchantWebService.setVar1("1");
        if (siParams!=null) merchantWebService.setVar5("1");
        merchantWebService.setVar2(cardbin.replace(" ", ""));

        merchantWebService.setHash(calculateHash("" + mPaymentParams.getKey() + "|" + PayuConstants.GET_BIN_INFO + "|" + 1 + "|" +salt));

        com.payu.india.Model.PostData postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();

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
            binInfoResponse = payuResponse;
            siParams.setCcCardType(getCardType(payuResponse.getCardInformation().getCardType()));
            siParams.setCcCategory(payuResponse.getCardInformation().getCardCategory());
            isSiSupported = payuResponse.getCardInformation().getIsSiSupported();
            if (isSiSupported){
                payNowButton.setEnabled(true);
            }else payNowButton.setEnabled(false);
        }
    }
    private String getCardType(String cardType)  {
        if(cardType.equalsIgnoreCase("CC"))
            return  "CC";
        else return "DC";
    }
}
