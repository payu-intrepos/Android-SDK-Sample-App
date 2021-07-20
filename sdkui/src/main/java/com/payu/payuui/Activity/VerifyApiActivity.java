package com.payu.payuui.Activity;

import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.payu.india.Extras.PayUChecksum;
import com.payu.india.Interfaces.CheckOfferDetailsApiListener;
import com.payu.india.Interfaces.DeleteCardApiListener;
import com.payu.india.Interfaces.DeleteCvvApiListener;
import com.payu.india.Interfaces.EditCardApiListener;
import com.payu.india.Interfaces.EligibleBinsForEMIApiListener;
import com.payu.india.Interfaces.GetCardInformationApiListener;
import com.payu.india.Interfaces.GetEmiAmountAccordingToInterestApiListener;
import com.payu.india.Interfaces.GetIbiboCodesApiListener;
import com.payu.india.Interfaces.GetOfferStatusApiListener;
import com.payu.india.Interfaces.GetStoredCardApiListener;
import com.payu.india.Interfaces.GetTransactionInfoApiListener;
import com.payu.india.Interfaces.LookupApiListener;
import com.payu.india.Interfaces.PaymentRelatedDetailsListener;
import com.payu.india.Interfaces.SaveCardApiListener;
import com.payu.india.Interfaces.ValueAddedServiceApiListener;
import com.payu.india.Interfaces.VerifyPaymentApiListener;
import com.payu.india.Model.EligibleEmiBins;
import com.payu.india.Model.LookupRequest;
import com.payu.india.Model.MerchantWebService;
import com.payu.india.Model.PaymentDetails;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuEmiAmountAccordingToInterest;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PayuResponse;
import com.payu.india.Model.StoredCard;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.Payu.PayuUtils;
import com.payu.india.PostParams.MerchantWebServicePostParams;
import com.payu.india.Tasks.CheckOfferDetailsTask;
import com.payu.india.Tasks.DeleteCardTask;
import com.payu.india.Tasks.EditCardTask;
import com.payu.india.Tasks.EligibleBinsForEMITask;
import com.payu.india.Tasks.GetCardInformationTask;
import com.payu.india.Tasks.GetEmiAmountAccordingToInterestTask;
import com.payu.india.Tasks.GetIbiboCodesTask;
import com.payu.india.Tasks.GetOfferStatusTask;
import com.payu.india.Tasks.GetPaymentRelatedDetailsTask;
import com.payu.india.Tasks.GetStoredCardTask;
import com.payu.india.Tasks.GetTransactionInfoTask;
import com.payu.india.Tasks.LookupTask;
import com.payu.india.Tasks.SaveCardTask;
import com.payu.india.Tasks.ValueAddedServiceTask;
import com.payu.india.Tasks.VerifyPaymentTask;
import com.payu.paymentparamhelper.PaymentParams;
import com.payu.paymentparamhelper.PostData;
import com.payu.payuui.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class VerifyApiActivity extends AppCompatActivity implements OnClickListener, GetStoredCardApiListener,
        SaveCardApiListener, EditCardApiListener, DeleteCardApiListener, GetCardInformationApiListener,
        GetIbiboCodesApiListener, ValueAddedServiceApiListener, GetTransactionInfoApiListener, GetOfferStatusApiListener, VerifyPaymentApiListener, DeleteCvvApiListener, CheckOfferDetailsApiListener, PaymentRelatedDetailsListener,
        GetEmiAmountAccordingToInterestApiListener, EligibleBinsForEMIApiListener, LookupApiListener {

    private Bundle bundle;

    private Button storeUserCardButton;
    private Button getUserCardsButton;
    private Button deleteUserCardButton;
    private Button editUserCardButton;
    private Button getCardInformationButton;
    private Button getIbiboCodeButton;
    private Button getValueAddedServiceButton;
    private Button getTransactionInformationButton;
    private Button getOfferStatusButton;
    private Button verifyTransactionButton;
    private Button deleteCvvButton;
    private Button checkOfferDetailsButton;
    private Button getEmiAmoutAccordingToInterest;
    private Button eligibleBinsForEMI;
    private Button lookupApi;

    private PayuHashes mPayuHashes;
    private MerchantWebService merchantWebService;
    private PaymentParams mPaymentParams;
    private PostData postData;

//    private PayuResponse mPayuResponse;

    private Boolean getUserCard = false;
    private Boolean editUserCard = false;
    private Boolean deleteUserCard = false;
    private StoredCard selectedUserCard;

    private ArrayList<StoredCard> storedCardsList;
    private ArrayList<PaymentDetails> netBankingList;

    private PayuConfig payuConfig;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_api);

        netBankingList = new ArrayList<PaymentDetails>();
        storedCardsList = new ArrayList<StoredCard>();

        // get the bundle variables
        bundle = getIntent().getExtras();
        mPayuHashes = bundle.getParcelable(PayuConstants.PAYU_HASHES);
        mPaymentParams = bundle.getParcelable(PayuConstants.PAYMENT_PARAMS);
        payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);

        netBankingList = bundle.getParcelableArrayList(PayuConstants.NETBANKING);
        storedCardsList = bundle.getParcelableArrayList(PayuConstants.STORED_CARD);

        if (null == netBankingList) {
            getPaymentRelatedDetails();
        }

        payuConfig = null != payuConfig ? payuConfig : new PayuConfig();
//        mPayuResponse = bundle.getParcelable(PayuConstants.PAYU_RESPONSE);

        // setup buttons
        (storeUserCardButton = (Button) findViewById(R.id.button_store_user_card)).setOnClickListener(this);
        (getUserCardsButton = (Button) findViewById(R.id.button_get_user_cards)).setOnClickListener(this);
        (deleteUserCardButton = (Button) findViewById(R.id.button_delete_user_card)).setOnClickListener(this);
        (editUserCardButton = (Button) findViewById(R.id.button_edit_user_card)).setOnClickListener(this);
        (getCardInformationButton = (Button) findViewById(R.id.button_get_card_information)).setOnClickListener(this);
        (getIbiboCodeButton = (Button) findViewById(R.id.button_get_ibibo_codes)).setOnClickListener(this);
        (getValueAddedServiceButton = (Button) findViewById(R.id.button_get_value_added_services)).setOnClickListener(this);
        (getTransactionInformationButton = (Button) findViewById(R.id.button_get_transaction_information)).setOnClickListener(this);
        (getOfferStatusButton = (Button) findViewById(R.id.button_get_offer_status)).setOnClickListener(this);
        (verifyTransactionButton = (Button) findViewById(R.id.button_verify_transaction)).setOnClickListener(this);
        (deleteCvvButton = (Button) findViewById(R.id.button_delete_cvv)).setOnClickListener(this);
        (checkOfferDetailsButton = (Button) findViewById(R.id.button_check_offer_details)).setOnClickListener(this);
        (getEmiAmoutAccordingToInterest = (Button) findViewById(R.id.button_get_emi_as_per_interest)).setOnClickListener(this);
        (eligibleBinsForEMI = (Button) findViewById(R.id.button_eligible_bins_for_emi)).setOnClickListener(this);
        (lookupApi = (Button) findViewById(R.id.button_lookup_request)).setOnClickListener(this);

    }

    @Override
    public void onLookupApiResponse(PayuResponse payuResponse) {
        Toast.makeText(this, "Response status:" + payuResponse.getLookupDetails().toString(), Toast.LENGTH_LONG).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_verify_api, menu);
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
    public void onClick(View v) {
        if (isOnline()) {
            if (v.getId() == R.id.button_store_user_card) {
                saveUserCard();
            } else if (v.getId() == R.id.button_delete_user_card) {
                deleteUserCard = true;
                editUserCard = false;
                getUserCard = false;
                deleteUserCard();
            } else if (v.getId() == R.id.button_edit_user_card) {
                deleteUserCard = false;
                editUserCard = true;
                getUserCard = false;
                editUserCard();
            } else if (v.getId() == R.id.button_get_user_cards) {
                deleteUserCard = false;
                editUserCard = false;
                getUserCard = true;
                getUserCard();
            } else if (v.getId() == R.id.button_get_card_information) {
                getCardInformation();
            } else if (v.getId() == R.id.button_get_ibibo_codes) {
                getIbiboCodes();
            } else if (v.getId() == R.id.button_get_value_added_services) {
                getValueAddedService();
            } else if (v.getId() == R.id.button_get_transaction_information) {
                getTransactionInformation();
            } else if (v.getId() == R.id.button_get_offer_status) {
                getOfferStatus();
            } else if (v.getId() == R.id.button_verify_transaction) {
                verifyPayment();
            } else if (v.getId() == R.id.button_delete_cvv) {
                deleteCvv();
            } else if (v.getId() == R.id.button_check_offer_details) {
                if (null != netBankingList)
                    checkOfferDetails();
                else
                    Toast.makeText(this, "not able to fetch netbanking details", Toast.LENGTH_LONG).show();
            } else if (v.getId() == R.id.button_get_emi_as_per_interest) {
                getEmiAmountAccordingToInterest();
            } else if (v.getId() == R.id.button_eligible_bins_for_emi) {
                eligibleBinsForEMI();
            } else if (v.getId() == R.id.button_lookup_request) {
                callLookupApi();
            }
        } else {
            Toast.makeText(getBaseContext(), "No Network Connectivity", Toast.LENGTH_SHORT).show();
        }
    }

    private void callLookupApi() {
        LayoutInflater layoutInflater = getLayoutInflater();
        View lookupRequest = layoutInflater.inflate(R.layout.layout_lookup_api, null);
        final EditText merchantAccessKey = lookupRequest.findViewById(R.id.payu_access_key);
        final EditText cardBin = lookupRequest.findViewById(R.id.payu_cardbin);
        final EditText amount = lookupRequest.findViewById(R.id.payu_amount);
        EditText currency = lookupRequest.findViewById(R.id.payu_currency);
        final EditText merchantOrderId = lookupRequest.findViewById(R.id.payu_order_id);
        EditText secretKey = lookupRequest.findViewById(R.id.payu_secret);
        Button btnSubmit = lookupRequest.findViewById(R.id.btn_submit);
        final String currencyValue = currency.getText().toString();
        final String merchantOrderIdValue = merchantOrderId.getText().toString();
        final String amountValue = amount.getText().toString();
        final String secretKeyValue = secretKey.getText().toString();
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(lookupRequest);
        dialog.setTitle("Enter Details");
        dialog.show();
        btnSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String hashString =currencyValue+merchantOrderIdValue+amountValue;
                String hash = calculateHmacSha1(hashString,secretKeyValue);
                LookupRequest lookupRequest = new LookupRequest.LookupApiRequestBuilder()
                        .setMerchantAccessKey(merchantAccessKey.getText().toString())
                        .setCardBin(cardBin.getText().toString())
                        .setAmount(amount.getText().toString())
                        .setCurrency(currencyValue)
                        .setProductType(LookupRequest.ProductType.MCP)
                        .setMerchantOrderId(merchantOrderId.getText().toString())
                        .setSignature(hash)
                        .build();
                String postData = lookupRequest.prepareJSON();
                payuConfig.setData(postData);
                LookupTask lookupTask = new LookupTask(VerifyApiActivity.this);
                lookupTask.execute(payuConfig);
                dialog.dismiss();
            }
        });

    }

    private String calculateHmacSha1(String hashString, String key) {
        try {
            String type = "HmacSHA1";
            SecretKeySpec secret = new SecretKeySpec(key.getBytes(), type);
            Mac mac = Mac.getInstance(type);
            mac.init(secret);
            byte[] bytes = mac.doFinal(hashString.getBytes());
            return getHexString(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private String getHexString(byte[] data) {
        // Create Hex String
        StringBuilder hexString = new StringBuilder();
        for (byte aMessageDigest : data) {
            String h = Integer.toHexString(0xFF & aMessageDigest);
            while (h.length() < 2)
                h = "0$h";
            hexString.append(h);
        }
        return hexString.toString();
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Sample implementation of getEmiAmountAccordingToInterest API
     */
    private void getEmiAmountAccordingToInterest() {
        LayoutInflater layoutInflater = getLayoutInflater();
        View viewEmiBankInput = layoutInflater.inflate(R.layout.layout_emi_interest, null);
        final EditText inputEditText = (EditText) viewEmiBankInput.findViewById(R.id.payu_emi_input);
        Button emiSubmitButton = (Button) viewEmiBankInput.findViewById(R.id.emi_submit_btn);

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(viewEmiBankInput);
        dialog.setTitle("Enter Amount");
        dialog.show();

        emiSubmitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                // Common for all the flow.
                merchantWebService = new MerchantWebService();
                // Setting merchant key or PARAM 1 of API
                merchantWebService.setKey(mPaymentParams.getKey());
                // Setting command OR PARAM 2 of API
                merchantWebService.setCommand(PayuConstants.API_GET_EMI_AMOUNT_ACCORDING_INTEREST);
                // Setting hash OR PARAM 3 of API
                // merchantWebService.setHash(mPayuHashes.getCheckOfferDetailsHash());

                // TODO Remove local hash generation code
                // Create hash locally
                PayUChecksum payUChecksum = new PayUChecksum();
                payUChecksum.setKey(mPaymentParams.getKey());
                payUChecksum.setCommand(PayuConstants.API_GET_EMI_AMOUNT_ACCORDING_INTEREST);
                payUChecksum.setVar1(inputEditText.getText().toString());
                payUChecksum.setSalt(bundle.getString(PayuConstants.SALT));

                if ((postData = payUChecksum.getHash()) != null && postData.getCode() == PayuErrors.NO_ERROR) {
                    // Setting hash OR PARAM 3 of API
                    merchantWebService.setHash(postData.getResult());
                }
                // Hash creation ends here
                // Setting var1 OR PARAM 4 of API
                merchantWebService.setVar1(inputEditText.getText().toString());
                // Getting postData in format accepted by API
                postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();
                // If there are no errors, proceed
                if (postData.getCode() == PayuErrors.NO_ERROR) {
                    // Setting data in payuConfig object
                    payuConfig.setData(postData.getResult());

                    // Fire API request
                    GetEmiAmountAccordingToInterestTask getEmiAmountAccordingToInterestTask = new GetEmiAmountAccordingToInterestTask(VerifyApiActivity.this);
                    getEmiAmountAccordingToInterestTask.execute(payuConfig);

                    dialog.dismiss();

                } else {
                    Toast.makeText(VerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    /**
     * Sample implementation of eligibleBinsForEMI API
     */
    private void eligibleBinsForEMI() {
        LayoutInflater layoutInflater = getLayoutInflater();
        View viewEmiBankInput = layoutInflater.inflate(R.layout.layout_eligible_bins_for_emi, null);
        final EditText var1 = viewEmiBankInput.findViewById(R.id.payu_var1);
        var1.setText(PayuConstants.DEFAULT);
        var1.setSelection(var1.getText().toString().length());
        final EditText cardBin = viewEmiBankInput.findViewById(R.id.payu_cardbin);
        final EditText bankName = viewEmiBankInput.findViewById(R.id.payu_bank_name);
        Button btnSubmit = viewEmiBankInput.findViewById(R.id.btn_submit);

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(viewEmiBankInput);
        dialog.setTitle("Enter Details");
        dialog.show();

        btnSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                // Common for all the flow.
                merchantWebService = new MerchantWebService();
                // Setting merchant key or PARAM 1 of API
                merchantWebService.setKey(mPaymentParams.getKey());
                // Setting command OR PARAM 2 of API
                merchantWebService.setCommand(PayuConstants.ELIGIBLE_BINS_FOR_EMI);

                // TODO Remove local hash generation code
                // Create hash locally
                PayUChecksum payUChecksum = new PayUChecksum();
                payUChecksum.setKey(mPaymentParams.getKey());
                payUChecksum.setCommand(PayuConstants.ELIGIBLE_BINS_FOR_EMI);
                payUChecksum.setVar1(var1.getText().toString());
                payUChecksum.setSalt(bundle.getString(PayuConstants.SALT));

                if ((postData = payUChecksum.getHash()) != null && postData.getCode() == PayuErrors.NO_ERROR) {
                    // Setting hash OR PARAM 3 of API
                    merchantWebService.setHash(postData.getResult());
                }
                // Hash creation ends here
                // Setting var1 OR PARAM 4 of API
                merchantWebService.setVar1(var1.getText().toString());

                if (!cardBin.getText().toString().isEmpty()) {
                    String bin = cardBin.getText().toString();

                    if (bin.length() > 6)
                        bin = bin.substring(0, 6);

                    merchantWebService.setVar2(bin);
                }

                if (!bankName.getText().toString().isEmpty())
                    merchantWebService.setVar3(bankName.getText().toString());

                // Getting postData in format accepted by API
                postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();
                // If there are no errors, proceed
                if (postData.getCode() == PayuErrors.NO_ERROR) {
                    // Setting data in payuConfig object
                    payuConfig.setData(postData.getResult());

                    // Fire API request
                    EligibleBinsForEMITask eligibleBinsForEMITask = new EligibleBinsForEMITask(VerifyApiActivity.this);
                    eligibleBinsForEMITask.execute(payuConfig);

                    dialog.dismiss();

                } else {
                    Toast.makeText(VerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    /**
     * check offer details.
     * can be used with card numebr, nb, user credential, bankcode.
     */
    private void checkOfferDetails() {
        LayoutInflater layoutInflater = getLayoutInflater();

        View viewOfferDetails = layoutInflater.inflate(R.layout.layout_check_offer_details, null);
        final EditText offerAmountEditText = (EditText) viewOfferDetails.findViewById(R.id.edit_text_offer_amount);


        final LinearLayout cardNumberLayout = (LinearLayout) viewOfferDetails.findViewById(R.id.linear_layout_card_number_layout);
        final EditText offerCardNumberEditText = (EditText) cardNumberLayout.findViewById(R.id.edit_text_offer_card_number);


        final LinearLayout userCredentialsLayout = (LinearLayout) viewOfferDetails.findViewById(R.id.linear_layout_user_credentials);
        final EditText userCredentialsEditText = (EditText) userCredentialsLayout.findViewById(R.id.edit_text_offer_user_credentials);
        final LinearLayout userCredentialsAndTokenLayout = (LinearLayout) viewOfferDetails.findViewById(R.id.linear_layout_user_credentials_and_card_token);
        final LinearLayout netBankingLayout = (LinearLayout) viewOfferDetails.findViewById(R.id.linear_layout_netbanking);
        final LinearLayout bankCodeLayout = (LinearLayout) viewOfferDetails.findViewById(R.id.linear_layout_bank_code);

        Spinner selectOfferModeSpinner = (Spinner) viewOfferDetails.findViewById(R.id.spinner_select_offer_mode);

        final Spinner netBankingSpinner = (Spinner) viewOfferDetails.findViewById(R.id.spinner_netbanking);
        final Spinner storedCardSpinner = (Spinner) viewOfferDetails.findViewById(R.id.spinner_stored_cards);

        // lets setup offer mode spinner.

        selectOfferModeSpinner.setAdapter(ArrayAdapter.createFromResource(this, R.array.check_offer_details_modes, android.R.layout.simple_spinner_item));
        selectOfferModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // new card
                        cardNumberLayout.setVisibility(VISIBLE);
                        userCredentialsLayout.setVisibility(GONE);
                        userCredentialsAndTokenLayout.setVisibility(GONE);
                        netBankingLayout.setVisibility(GONE);
                        bankCodeLayout.setVisibility(GONE);
                        break;
                    case 1: // user credentials
                        cardNumberLayout.setVisibility(GONE);
                        userCredentialsLayout.setVisibility(VISIBLE);
                        userCredentialsAndTokenLayout.setVisibility(GONE);
                        netBankingLayout.setVisibility(GONE);
                        bankCodeLayout.setVisibility(GONE);

                        userCredentialsEditText.setText(mPaymentParams.getUserCredentials() == null ? "" : mPaymentParams.getUserCredentials());
                        break;
                    case 2: // user credentials and card token
                        cardNumberLayout.setVisibility(GONE);
                        userCredentialsLayout.setVisibility(GONE);
                        userCredentialsAndTokenLayout.setVisibility(VISIBLE);
                        netBankingLayout.setVisibility(GONE);
                        bankCodeLayout.setVisibility(GONE);
                        break;
                    case 3: // net banking
                        cardNumberLayout.setVisibility(GONE);
                        userCredentialsLayout.setVisibility(GONE);
                        userCredentialsAndTokenLayout.setVisibility(GONE);
                        netBankingLayout.setVisibility(VISIBLE);
                        bankCodeLayout.setVisibility(GONE);
                        break;
                    case 4: // net banking and bank code
                        cardNumberLayout.setVisibility(GONE);
                        userCredentialsLayout.setVisibility(GONE);
                        userCredentialsAndTokenLayout.setVisibility(GONE);
                        netBankingLayout.setVisibility(GONE);
                        bankCodeLayout.setVisibility(VISIBLE);
                        break;
                    default: // new card
                        cardNumberLayout.setVisibility(VISIBLE);
                        userCredentialsLayout.setVisibility(GONE);
                        userCredentialsAndTokenLayout.setVisibility(GONE);
                        netBankingLayout.setVisibility(GONE);
                        bankCodeLayout.setVisibility(GONE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // setup net banking spinner.
        BaseAdapter netBankingAdapter = new PayUNetBankingAdapter(this, netBankingList);
        netBankingSpinner.setAdapter(netBankingAdapter);

        // setup stored card spinner.
        final BaseAdapter storedCardsAdapter = new UserCardsAdapter(this, storedCardsList);
        storedCardSpinner.setAdapter(storedCardsAdapter);


        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.cb_dialog);
        builder.setView(viewOfferDetails).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        final AlertDialog checkOfferDetails = builder.create();
        checkOfferDetails.show();

        checkOfferDetails.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Common for all the flow.
                merchantWebService = new MerchantWebService();
                merchantWebService.setKey(mPaymentParams.getKey());
                merchantWebService.setCommand(PayuConstants.CHECK_OFFER_DETAILS);
                merchantWebService.setHash(mPayuHashes.getCheckOfferDetailsHash());

//                PayUChecksum payUChecksum = new PayUChecksum();
//                payUChecksum.setKey(mPaymentParams.getKey());
//                payUChecksum.setCommand(PayuConstants.CHECK_OFFER_DETAILS);
//                payUChecksum.setVar1(mPaymentParams.getOfferKey());
//                payUChecksum.setSalt(bundle.getString(PayuConstants.SALT));
//                if ((postData = payUChecksum.getHash()) != null && postData.getCode() == PayuErrors.NO_ERROR) {
//                    merchantWebService.setHash(postData.getResult());
//                }

                merchantWebService.setVar1(mPaymentParams.getOfferKey());

                if (cardNumberLayout.getVisibility() == VISIBLE) { // card number mode
                    merchantWebService.setVar2("" + 1);
                    merchantWebService.setVar3(offerCardNumberEditText.getText().toString());
                } else if (userCredentialsLayout.getVisibility() == VISIBLE) { // user credentials mode
                    merchantWebService.setVar2("" + 2);
                    merchantWebService.setVar3(mPaymentParams.getUserCredentials());
                } else if (userCredentialsAndTokenLayout.getVisibility() == VISIBLE) { // user credentials and card token mode
                    merchantWebService.setVar2("" + 2);
                    merchantWebService.setVar3(mPaymentParams.getUserCredentials());
                    merchantWebService.setVar4(storedCardsList.get(storedCardSpinner.getSelectedItemPosition()).getCardToken());
                } else if (netBankingLayout.getVisibility() == VISIBLE) { // nb mode
                    merchantWebService.setVar2("" + 3);
                    merchantWebService.setVar3("NB");
                } else if (bankCodeLayout.getVisibility() == VISIBLE) { // bank code mode
                    merchantWebService.setVar2("" + 4);
                    merchantWebService.setVar3(netBankingList.get(netBankingSpinner.getSelectedItemPosition()).getBankCode());
                }
                merchantWebService.setVar5(offerAmountEditText.getText().toString());

                postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();

                if (postData.getCode() == PayuErrors.NO_ERROR) {
                    payuConfig.setData(postData.getResult());

                    CheckOfferDetailsTask checkOfferDetailsTask = new CheckOfferDetailsTask(VerifyApiActivity.this);
                    checkOfferDetailsTask.execute(payuConfig);

                    // lets cancel the dialog.
                    checkOfferDetails.dismiss();
                } else {
                    Toast.makeText(VerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void getUserCard() {
        MerchantWebService merchantWebService = new MerchantWebService();
        merchantWebService.setKey(mPaymentParams.getKey());
        merchantWebService.setCommand(PayuConstants.GET_USER_CARDS);
        merchantWebService.setVar1(mPaymentParams.getUserCredentials());
        merchantWebService.setHash(mPayuHashes.getStoredCardsHash());

        postData = null;
        postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();

        if (postData.getCode() == PayuErrors.NO_ERROR) {
            // okay we have post data now, lets make the api call
            payuConfig.setData(postData.getResult());
            GetStoredCardTask getStoredCardTask = new GetStoredCardTask(this);
            getStoredCardTask.execute(payuConfig);
        } else {
            Toast.makeText(this, postData.getResult(), Toast.LENGTH_LONG).show();
        }
    }

    private void editUserCard() {
        // get the user cards first, there we decide
        getUserCard();
    }

    private void deleteUserCard() {
        getUserCard();
    }

    private void saveUserCard() {
        LayoutInflater layoutInflater = getLayoutInflater();
        View viewStoreUserCard = layoutInflater.inflate(R.layout.layout_store_user_cards, null);
        final EditText cardNumberEditText = (EditText) viewStoreUserCard.findViewById(R.id.edit_text_card_number);
        final EditText cardHolderNameEditText = (EditText) viewStoreUserCard.findViewById(R.id.edit_text_card_holder_name);
        final EditText cardExpiryMonthEditText = (EditText) viewStoreUserCard.findViewById(R.id.edit_text_expiry_month);
        final EditText cardExpiryYearEditText = (EditText) viewStoreUserCard.findViewById(R.id.edit_text_expiry_year);
        final EditText cvvEditText = (EditText) viewStoreUserCard.findViewById(R.id.edit_text_cvv);
        final EditText cardNameEditText = (EditText) viewStoreUserCard.findViewById(R.id.edit_text_card_name);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.cb_dialog);
        builder.setView(viewStoreUserCard).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        final AlertDialog saveUserCardDialog = builder.create();
        saveUserCardDialog.show();

        // lets handle the positive button click!
        saveUserCardDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO make api call and store the card

                // Todo make add all the validations.
                merchantWebService = new MerchantWebService();
                merchantWebService.setKey(mPaymentParams.getKey());
                merchantWebService.setCommand(PayuConstants.SAVE_USER_CARD);
                merchantWebService.setHash(mPayuHashes.getSaveCardHash());
                merchantWebService.setVar1(mPaymentParams.getUserCredentials());
                merchantWebService.setVar2("" + cardHolderNameEditText.getText().toString());
                merchantWebService.setVar3(PayuConstants.CC);
                merchantWebService.setVar4(PayuConstants.CC);
                merchantWebService.setVar5("" + cardNameEditText.getText().toString());
                merchantWebService.setVar6("" + cardNumberEditText.getText().toString());
                merchantWebService.setVar7("" + cardExpiryMonthEditText.getText().toString());
                merchantWebService.setVar8("" + cardExpiryYearEditText.getText().toString());

                postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();

                if (postData.getCode() == PayuErrors.NO_ERROR) {
                    payuConfig.setData(postData.getResult());

                    SaveCardTask saveCardTask = new SaveCardTask(VerifyApiActivity.this);
                    saveCardTask.execute(payuConfig);

                    // lets cancel the dialog.
                    saveUserCardDialog.dismiss();
                } else {
                    Toast.makeText(VerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void showDeleteUserCardsDialog(ArrayList<StoredCard> userCards) {
        selectedUserCard = null;
        LayoutInflater layoutInflater = getLayoutInflater();
        View viewDeleteUserCard = layoutInflater.inflate(R.layout.layout_delete_user_cards, null);
        ListView deleteUserCardListView = (ListView) viewDeleteUserCard.findViewById(R.id.list_view_delete_user_card);


        UserCardsAdapter userCardsAdapter = new UserCardsAdapter(this, userCards);
        deleteUserCardListView.setAdapter(userCardsAdapter);

        deleteUserCardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedUserCard = (StoredCard) parent.getItemAtPosition(position);
            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(viewDeleteUserCard).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (null != selectedUserCard) {
                    MerchantWebService merchantWebService = new MerchantWebService();
                    merchantWebService.setKey(mPaymentParams.getKey());
                    merchantWebService.setCommand(PayuConstants.DELETE_USER_CARD);
                    merchantWebService.setVar1(mPaymentParams.getUserCredentials());
                    merchantWebService.setVar2(selectedUserCard.getCardToken());
                    merchantWebService.setHash(mPayuHashes.getDeleteCardHash());

                    postData = null;
                    postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();

                    if (postData.getCode() == PayuErrors.NO_ERROR) {
                        // ok we got the post params, let make an api call to payu to fetch the payment related details

                        payuConfig.setData(postData.getResult());

                        DeleteCardTask deleteCardTask = new DeleteCardTask(VerifyApiActivity.this);
                        deleteCardTask.execute(payuConfig);
                    } else {
                        Toast.makeText(VerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog deleteUserCardDialog = builder.create();
        deleteUserCardDialog.show();
    }

    private void showEditUserCardsDialog(ArrayList<StoredCard> userCards) {

        selectedUserCard = null;

        LayoutInflater layoutInflater = getLayoutInflater();
        View viewEditUserCard = layoutInflater.inflate(R.layout.layout_edit_user_cards, null);
        Spinner userCardsSpinner = (Spinner) viewEditUserCard.findViewById(R.id.spinner_edit_card);

        UserCardsAdapter userCardsAdapter = new UserCardsAdapter(this, userCards);

        userCardsSpinner.setAdapter(userCardsAdapter);

        final EditText cardNumberEditText = (EditText) viewEditUserCard.findViewById(R.id.edit_text_card_number);
        final EditText cardHolderNameEditText = (EditText) viewEditUserCard.findViewById(R.id.edit_text_card_holder_name);
        final EditText cardExpiryMonthEditText = (EditText) viewEditUserCard.findViewById(R.id.edit_text_expiry_month);
        final EditText cardExpiryYearEditText = (EditText) viewEditUserCard.findViewById(R.id.edit_text_expiry_year);
        final EditText cardNameEditText = (EditText) viewEditUserCard.findViewById(R.id.edit_text_card_name);

        //TODO: set adaptor to spListOfUserCards
        userCardsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedUserCard = (StoredCard) adapterView.getSelectedItem();
                // oops i dont have the card number. user have to type the card number.
                cardNumberEditText.setHint(selectedUserCard.getMaskedCardNumber());
                cardHolderNameEditText.setText(selectedUserCard.getNameOnCard());
                cardNameEditText.setText(selectedUserCard.getCardName());
                cardExpiryMonthEditText.setText(selectedUserCard.getExpiryMonth());
                cardExpiryYearEditText.setText(selectedUserCard.getExpiryYear());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(viewEditUserCard).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        final AlertDialog editUserCardDialog = builder.create();
        editUserCardDialog.show();

        editUserCardDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != selectedUserCard) {
                    merchantWebService = new MerchantWebService();
                    merchantWebService.setKey(mPaymentParams.getKey());
                    merchantWebService.setCommand(PayuConstants.EDIT_USER_CARD);
                    merchantWebService.setHash(mPayuHashes.getEditCardHash());
                    merchantWebService.setVar1(mPaymentParams.getUserCredentials());
                    merchantWebService.setVar2(selectedUserCard.getCardToken());
                    merchantWebService.setVar3(cardNameEditText.getText().toString());
                    merchantWebService.setVar4(selectedUserCard.getCardMode());
                    merchantWebService.setVar5(selectedUserCard.getCardType());
                    merchantWebService.setVar6(cardHolderNameEditText.getText().toString());
                    merchantWebService.setVar7(cardNumberEditText.getText().toString());
                    merchantWebService.setVar8(cardExpiryMonthEditText.getText().toString());
                    merchantWebService.setVar9(cardExpiryYearEditText.getText().toString());

                    postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();

                    if (postData.getCode() == PayuErrors.NO_ERROR) {
                        payuConfig.setData(postData.getResult());

                        EditCardTask editCardTask = new EditCardTask(VerifyApiActivity.this);
                        editCardTask.execute(payuConfig);

                        // lets cancel the dialog.
                        editUserCardDialog.dismiss();
                    } else {
                        Toast.makeText(VerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void showUserCardsDialog(ArrayList<StoredCard> userCards) {

        LayoutInflater layoutInflater = getLayoutInflater();
        View viewUserCards = layoutInflater.inflate(R.layout.layout_get_user_cards, null);
        ListView listView = (ListView) viewUserCards.findViewById(R.id.list_view_user_card);
        UserCardsAdapter userCardsAdapter = new UserCardsAdapter(this, userCards);
        listView.setAdapter(userCardsAdapter);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(viewUserCards)
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }


    private void getCardInformation() {
        LayoutInflater layoutInflater = getLayoutInflater();
        View viewCardInforamtion = layoutInflater.inflate(R.layout.layout_get_card_information, null);
        final EditText cardNumberEditText = (EditText) viewCardInforamtion.findViewById(R.id.edit_text_card_number);
        final PayuUtils payuUtils = new PayuUtils();

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(viewCardInforamtion)
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        final AlertDialog getCardInfomationAlertDialog = builder.create();
        getCardInfomationAlertDialog.show();

        getCardInfomationAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String cardNumber;
                // validate the card number! if valid then proceed else show a toast message.
                if ((cardNumber = cardNumberEditText.getText().toString()) != null && payuUtils.validateCardNumber(cardNumber)) {

                    PayUChecksum payUChecksum = new PayUChecksum();
                    payUChecksum.setKey(mPaymentParams.getKey());
                    payUChecksum.setCommand(PayuConstants.CHECK_IS_DOMESTIC);
                    // var 1 is from date
                    payUChecksum.setVar1(cardNumberEditText.getText().toString().substring(0, 6));
                    payUChecksum.setSalt(bundle.getString(PayuConstants.SALT));

                    MerchantWebService merchantWebService = new MerchantWebService();
                    merchantWebService.setCommand(PayuConstants.CHECK_IS_DOMESTIC);
                    merchantWebService.setKey(mPaymentParams.getKey());
                    // merchantWebService.setHash(mPayuHashes.getCheckIsDomesticHash());
                    merchantWebService.setHash(payUChecksum.getHash().getResult());
                    // we need only the card bin. lets find the first 6 digits.
                    merchantWebService.setVar1(cardNumberEditText.getText().toString().substring(0, 6));
                    MerchantWebServicePostParams merchantWebServicePostParams = new MerchantWebServicePostParams(merchantWebService);
                    postData = merchantWebServicePostParams.getMerchantWebServicePostParams();
                    if (postData.getCode() == PayuErrors.NO_ERROR) {
                        payuConfig.setData(postData.getResult());
                        GetCardInformationTask getCardInformationTask = new GetCardInformationTask(VerifyApiActivity.this);
                        getCardInformationTask.execute(payuConfig);
                        getCardInfomationAlertDialog.dismiss();
                    } else {
                        Toast.makeText(VerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(VerifyApiActivity.this, "Invalid card number", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showValueAddedServiceResponse(PayuResponse payuResponse) {
        if (null != payuResponse && (payuResponse.isIssuingBankStatusAvailable() || payuResponse.isNetBankingStatusAvailable())) {
            StringBuffer issuingBankStatus = new StringBuffer();
            for (String key : payuResponse.getIssuingBankStatus().keySet()) {
                issuingBankStatus.append("Bank code: " + key + "\n");
                issuingBankStatus.append("Bank Name: " + payuResponse.getIssuingBankStatus().get(key).getBankName() + "\n");
                issuingBankStatus.append("Bank status: " + payuResponse.getIssuingBankStatus().get(key).getStatusCode() + "\n");
            }
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage("Netbanking status: \n" + payuResponse.getNetBankingDownStatus().toString() + "issuing bank status: \n" + issuingBankStatus)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    }).show();
        } else if (null != payuResponse && null != payuResponse.getResponseStatus()) {
            Toast.makeText(VerifyApiActivity.this, payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(VerifyApiActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
        }
    }

    private void getIbiboCodes() {
        MerchantWebService merchantWebService = new MerchantWebService();
        merchantWebService.setKey(mPaymentParams.getKey());
        merchantWebService.setCommand(PayuConstants.GET_MERCHANT_IBIBO_CODES);
        merchantWebService.setVar1(PayuConstants.DEFAULT);
        merchantWebService.setHash(mPayuHashes.getMerchantIbiboCodesHash());

        PostData postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();
        if (postData.getCode() == PayuErrors.NO_ERROR) {
            payuConfig.setData(postData.getResult());
            GetIbiboCodesTask getIbiboCodesTask = new GetIbiboCodesTask(this);
            getIbiboCodesTask.execute(payuConfig);
        } else {
            Toast.makeText(VerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Get payment related details like netbanking, stored cards, emi etc.
     */
    private void getPaymentRelatedDetails() {
        MerchantWebService merchantWebService = new MerchantWebService();
        merchantWebService.setKey(mPaymentParams.getKey());
        merchantWebService.setCommand(PayuConstants.PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK);
        merchantWebService.setVar1(mPaymentParams.getUserCredentials() == null ? PayuConstants.DEFAULT : mPaymentParams.getUserCredentials());
        merchantWebService.setHash(mPayuHashes.getPaymentRelatedDetailsForMobileSdkHash());

        PostData postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();
        if (postData.getCode() == PayuErrors.NO_ERROR) {
            payuConfig.setData(postData.getResult());
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Fetching bank information please wait.");
            progressDialog.show();
            GetPaymentRelatedDetailsTask getPaymentRelatedDetailsTask = new GetPaymentRelatedDetailsTask(this);
            getPaymentRelatedDetailsTask.execute(payuConfig);
        } else {
            Toast.makeText(VerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
        }
    }

    private void getValueAddedService() {
        MerchantWebService merchantWebService = new MerchantWebService();
        merchantWebService.setKey(mPaymentParams.getKey());
        merchantWebService.setCommand(PayuConstants.VAS_FOR_MOBILE_SDK);
        merchantWebService.setHash(mPayuHashes.getVasForMobileSdkHash());
        merchantWebService.setVar1(PayuConstants.DEFAULT);
        merchantWebService.setVar2(PayuConstants.DEFAULT);
        merchantWebService.setVar3(PayuConstants.DEFAULT);

        if ((postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams()) != null && postData.getCode() == PayuErrors.NO_ERROR) {
            payuConfig.setData(postData.getResult());
            ValueAddedServiceTask valueAddedServiceTask = new ValueAddedServiceTask(this);
            valueAddedServiceTask.execute(payuConfig);
        } else {
            Toast.makeText(this, postData.getResult(), Toast.LENGTH_LONG).show();
        }
    }

    private void getTransactionInformation() { // TODO gotta fix, not working, ui visibility is gone now.
        // lets create a popup

        LayoutInflater layoutInflater = getLayoutInflater();
        View layoutGetTransactionInformation = layoutInflater.inflate(R.layout.layout_get_transaction_information, null);

        final EditText fromDateEditText = (EditText) layoutGetTransactionInformation.findViewById(R.id.edit_text_from_date);
        EditText toDateEditText = (EditText) layoutGetTransactionInformation.findViewById(R.id.edit_text_to_date);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layoutGetTransactionInformation)
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        final AlertDialog getTransactionInformationAlertDialog = builder.create();
        getTransactionInformationAlertDialog.show();

        getTransactionInformationAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // we gotta calculate hash at our end
                PayUChecksum payUChecksum = new PayUChecksum();
                payUChecksum.setKey(mPaymentParams.getKey());
                payUChecksum.setCommand(PayuConstants.GET_TRANSACTION_INFO);
                // var 1 is from date
                payUChecksum.setVar1(fromDateEditText.getText().toString());
                payUChecksum.setSalt(bundle.getString(PayuConstants.SALT));
                if ((postData = payUChecksum.getHash()) != null && postData.getCode() == PayuErrors.NO_ERROR) {
                    MerchantWebService merchantWebService = new MerchantWebService();
                    merchantWebService.setKey(mPaymentParams.getKey());
                    merchantWebService.setCommand(PayuConstants.GET_TRANSACTION_INFO);
                    merchantWebService.setHash(postData.getResult());
                    merchantWebService.setVar1(fromDateEditText.getText().toString());
                    merchantWebService.setVar2(fromDateEditText.getText().toString());
                    if ((postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams()) != null && postData.getCode() == PayuErrors.NO_ERROR) {
                        payuConfig.setData(postData.getResult());

                        // lets make api call
                        GetTransactionInfoTask getTransactionInfoTask = new GetTransactionInfoTask(VerifyApiActivity.this);
                        getTransactionInfoTask.execute(payuConfig);

                        getTransactionInformationAlertDialog.dismiss();

                    } else {
                        Toast.makeText(VerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(VerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void getOfferStatus() {
        LayoutInflater layoutInflater = getLayoutInflater();
        View viewOfferStatus = layoutInflater.inflate(R.layout.layout_get_offer_status, null);

        final EditText offerCardNumberEditText = (EditText) viewOfferStatus.findViewById(R.id.edit_text_offer_card_number);
        final EditText offerNameOnCardEditText = (EditText) viewOfferStatus.findViewById(R.id.edit_text_offer_name_on_card);
        final EditText offerPaymentModeEditText = (EditText) viewOfferStatus.findViewById(R.id.edit_text_offer_card_mode);

        final LinearLayout newCardLinearLayout = (LinearLayout) viewOfferStatus.findViewById(R.id.linear_layout_new_card);
        final LinearLayout netBankingLinearLayout = (LinearLayout) viewOfferStatus.findViewById(R.id.linear_layout_netbanking);
        final LinearLayout storedCardLinearLayout = (LinearLayout) viewOfferStatus.findViewById(R.id.linear_layout_stored_card);


        Spinner selectOfferModeSpinner = (Spinner) viewOfferStatus.findViewById(R.id.spinner_select_offer_mode);

        final Spinner netBankingSpinner = (Spinner) viewOfferStatus.findViewById(R.id.spinner_netbanking);
        final Spinner storedCardSpinner = (Spinner) viewOfferStatus.findViewById(R.id.spinner_stored_cards);

        // lets setup offer mode spinner.

        selectOfferModeSpinner.setAdapter(ArrayAdapter.createFromResource(this, R.array.offer_modes, android.R.layout.simple_spinner_item));
        selectOfferModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // new card
                        newCardLinearLayout.setVisibility(VISIBLE);
                        storedCardLinearLayout.setVisibility(GONE);
                        netBankingLinearLayout.setVisibility(GONE);
                        break;
                    case 1: // stored card
                        newCardLinearLayout.setVisibility(GONE);
                        storedCardLinearLayout.setVisibility(VISIBLE);
                        netBankingLinearLayout.setVisibility(GONE);
                        break;
                    case 2: // net banking
                        newCardLinearLayout.setVisibility(GONE);
                        storedCardLinearLayout.setVisibility(GONE);
                        netBankingLinearLayout.setVisibility(VISIBLE);
                        break;
                    default: // new card
                        newCardLinearLayout.setVisibility(VISIBLE);
                        storedCardLinearLayout.setVisibility(GONE);
                        netBankingLinearLayout.setVisibility(GONE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // setup net banking spinner.
        BaseAdapter netBankingAdapter = new PayUNetBankingAdapter(this, netBankingList);
        netBankingSpinner.setAdapter(netBankingAdapter);

        // setup stored card spinner.
        final BaseAdapter storedCardsAdapter = new UserCardsAdapter(this, storedCardsList);
        storedCardSpinner.setAdapter(storedCardsAdapter);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(viewOfferStatus).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        final AlertDialog getOfferDetailsDialog = builder.create();
        getOfferDetailsDialog.show();

        getOfferDetailsDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Common for all the flow.
                merchantWebService = new MerchantWebService();
                merchantWebService.setKey(mPaymentParams.getKey());
                merchantWebService.setCommand(PayuConstants.CHECK_OFFER_STATUS);
                merchantWebService.setHash(mPayuHashes.getCheckOfferStatusHash());
                merchantWebService.setVar1(mPaymentParams.getOfferKey());
                merchantWebService.setVar2(mPaymentParams.getAmount());

                if (netBankingLinearLayout.getVisibility() == VISIBLE) { // its netbanking flow.
                    merchantWebService.setVar3("NB"); // mode
                    merchantWebService.setVar4(netBankingList.get(netBankingSpinner.getSelectedItemPosition()).getBankCode());
                }

                if (newCardLinearLayout.getVisibility() == VISIBLE) {

                    merchantWebService.setVar3(offerPaymentModeEditText.getText().toString());
                    merchantWebService.setVar4(offerCardNumberEditText.getText().toString().startsWith("4") ? "VISA" : "MAST");
                    // Required only for new card
                    merchantWebService.setVar5(offerCardNumberEditText.getText().toString());
                    // Optional
                    merchantWebService.setVar6(offerNameOnCardEditText.getText().toString());

                } else {
                    merchantWebService.setVar5("");
                    merchantWebService.setVar6("");
                }

                merchantWebService.setVar7(mPaymentParams.getPhone());
                merchantWebService.setVar8(mPaymentParams.getEmail());
                merchantWebService.setVar9("");

                // Needed only in case of stored card
                if (storedCardLinearLayout.getVisibility() == VISIBLE) {
                    merchantWebService.setVar3(storedCardsList.get(storedCardSpinner.getSelectedItemPosition()).getCardMode().toUpperCase());
                    merchantWebService.setVar4(storedCardsList.get(storedCardSpinner.getSelectedItemPosition()).getCardBin().startsWith("4") ? "VISA" : "MAST");
                    merchantWebService.setVar10(mPaymentParams.getUserCredentials());
                    merchantWebService.setVar11(storedCardsList.get(storedCardSpinner.getSelectedItemPosition()).getCardToken());
                }
                postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();

                if (postData.getCode() == PayuErrors.NO_ERROR) {
                    payuConfig.setData(postData.getResult());

                    GetOfferStatusTask getOfferStatusTask = new GetOfferStatusTask(VerifyApiActivity.this);
                    getOfferStatusTask.execute(payuConfig);

                    // lets cancel the dialog.
                    getOfferDetailsDialog.dismiss();
                } else {
                    Toast.makeText(VerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void verifyPayment() {
        LayoutInflater layoutInflater = getLayoutInflater();
        View layoutVerifyPayment = layoutInflater.inflate(R.layout.layout_verify_payment, null);

        final EditText verifyPaymentTransactionIdEditText = (EditText) layoutVerifyPayment.findViewById(R.id.edit_text_verify_payment_transaction_id);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layoutVerifyPayment)
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        final AlertDialog verifyPaymentDialog = builder.create();
        verifyPaymentDialog.show();

        verifyPaymentDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // we gotta calculate hash at our end
                PayUChecksum payUChecksum = new PayUChecksum();
                payUChecksum.setKey(mPaymentParams.getKey());
                payUChecksum.setCommand(PayuConstants.VERIFY_PAYMENT);
                // var 1 is from date
                payUChecksum.setVar1(verifyPaymentTransactionIdEditText.getText().toString());
                payUChecksum.setSalt(bundle.getString(PayuConstants.SALT));
                if ((postData = payUChecksum.getHash()) != null && postData.getCode() == PayuErrors.NO_ERROR) {
                    MerchantWebService merchantWebService = new MerchantWebService();
                    merchantWebService.setKey(mPaymentParams.getKey());
                    merchantWebService.setCommand(PayuConstants.VERIFY_PAYMENT);
                    merchantWebService.setHash(postData.getResult());
                    merchantWebService.setVar1(verifyPaymentTransactionIdEditText.getText().toString());
                    if ((postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams()) != null && postData.getCode() == PayuErrors.NO_ERROR) {
                        payuConfig.setData(postData.getResult());

                        // lets make api call
                        VerifyPaymentTask verifyPaymentTask = new VerifyPaymentTask(VerifyApiActivity.this);
                        verifyPaymentTask.execute(payuConfig);

                        verifyPaymentDialog.dismiss();

                    } else {
                        Toast.makeText(VerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(VerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void deleteCvv() {
        LayoutInflater layoutInflater = getLayoutInflater();
        View viewDeleteCardCvv = layoutInflater.inflate(R.layout.layout_delete_stored_card_cvv, null);
        ListView deleteCardCvvListView = (ListView) viewDeleteCardCvv.findViewById(R.id.list_view_delete_stored_card_cvv);

//        ArrayList<StoredCard> oneTapCard= new ArrayList<>();
//        for(StoredCard storedCard: storedCardsList){
//            if(storedCard.getOneTapCard()!=0)
//                oneTapCard.add(storedCard);
//        }
//        if(oneTapCard.size()>0) {
//            UserCardsAdapter userCardsAdapter = new UserCardsAdapter(this, oneTapCard);
//            deleteCardCvvListView.setAdapter(userCardsAdapter);
//
//            deleteCardCvvListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    selectedUserCard = (StoredCard) parent.getItemAtPosition(position);
//                }
//            });
//
//            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setView(viewDeleteCardCvv).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    if (null != selectedUserCard) {
//                        MerchantWebService merchantWebService = new MerchantWebService();
//                        merchantWebService.setKey(mPaymentParams.getKey());
//                        merchantWebService.setCommand(PayuConstants.DELETE_STORE_CARD_CVV);
//                        merchantWebService.setVar1(mPaymentParams.getUserCredentials());
//                        merchantWebService.setVar2(selectedUserCard.getCardToken());
//                        merchantWebService.setHash(mPayuHashes.getDeleteStoreCardCvv());
//
//                        postData = null;
//                        postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();
//
//                        if (postData.getCode() == PayuErrors.NO_ERROR) {
//                            // ok we got the post params, let make an api call to payu to fetch the payment related details
//
//                            payuConfig.setData(postData.getResult());
//
//                            DeleteCvvTask deleteCvvTask = new DeleteCvvTask(VerifyApiActivity.this);
//                            deleteCvvTask.execute(payuConfig);
//                        } else {
//                            Toast.makeText(VerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
//                        }
//                    }
//                }
//            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//
//                }
//            });
//
//            AlertDialog deleteUserCardDialog = builder.create();
//            deleteUserCardDialog.show();
//        }else{
//            Toast.makeText(this, "No one tap card", Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    public void onGetStoredCardApiResponse(PayuResponse payuResponse) {
        // now we got response, here we build an alert and show the list of user cards.
        if (payuResponse.getResponseStatus().getCode() == PayuErrors.NO_ERROR) {
            if (getUserCard)
                showUserCardsDialog(payuResponse.getStoredCards());
            else if (editUserCard)
                showEditUserCardsDialog(payuResponse.getStoredCards());
            else if (deleteUserCard)
                showDeleteUserCardsDialog(payuResponse.getStoredCards());
        } else {
            Toast.makeText(this, payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSaveCardResponse(PayuResponse payuResponse) {
        // lets verify the
        Toast.makeText(this, payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onEditCardApiListener(PayuResponse payuResponse) {
        Toast.makeText(this, payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDeleteCardApiResponse(PayuResponse payuResponse) {
        Toast.makeText(this, payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGetCardInformationResponse(PayuResponse payuResponse) {
        if (payuResponse.isCardInformationAvailable()) {
            Toast.makeText(VerifyApiActivity.this, "Is Domestic: " + payuResponse.getCardInformation().getIsDomestic() + " Issuing Bank: " + payuResponse.getCardInformation().getIssuingBank() + " Card Type: " + payuResponse.getCardInformation().getCardType() + " Card Category: " + payuResponse.getCardInformation().getCardCategory(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onGetIbiboCodesApiResponse(PayuResponse payuResponse) {
        Toast.makeText(this, "Response status: " + payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onValueAddedServiceApiResponse(PayuResponse payuResponse) {
//        Toast.makeText(this, "Response status: " + payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
        showValueAddedServiceResponse(payuResponse);
    }

    @Override
    public void onGetTransactionApiListener(PayuResponse payuResponse) {
        Toast.makeText(this, "Response status: " + payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGetOfferStatusApiResponse(PayuResponse payuResponse) {
        Toast.makeText(this, "Response status: " + payuResponse.getResponseStatus().getResult() + ": Discount = " + payuResponse.getPayuOffer().getDiscount(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onVerifyPaymentResponse(PayuResponse payuResponse) {
        Toast.makeText(this, "Response status:" + payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDeleteCvvApiResponse(PayuResponse payuResponse) {

    }

    @Override
    public void onPaymentRelatedDetailsResponse(PayuResponse payuResponse) {
        netBankingList = payuResponse.getNetBanks();
        storedCardsList = payuResponse.getStoredCards();
        progressDialog.dismiss();
    }

    @Override
    public void onCheckOfferDetailsApiResponse(PayuResponse payuResponse) {
        if (null != payuResponse) {
            Toast.makeText(this, payuResponse.getResponseStatus().getStatus(), Toast.LENGTH_LONG).show();
            StringBuffer stringBuffer = new StringBuffer();
           /* if (payuResponse.getOfferDetailsList() != null) {
                PayuOffer offer;
                for (int i = 0, length = payuResponse.getOfferDetailsList().size(); i < length; i++) {
                    offer = payuResponse.getOfferDetailsList().get(i);
                    stringBuffer.append(" Category: ").append(offer.getCategory()).append(" discount: ").append(offer.getDiscount()).append(" message: ").append(offer.getMsg()).append(" ");
                }
            } else {
                stringBuffer.append(payuResponse.getResponseStatus().getResult());
            }

            new android.app.AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage(stringBuffer)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    }).show();*/
        }
    }

    /**
     * API Response Listener: getEmiAmountAccordingToInterest
     *
     * @param payuResponse {@link PayuResponse#getResponseStatus()}
     */
    @Override
    public void onGetEmiAmountAccordingToInterestApiResponse(final PayuResponse payuResponse) {
        // ################## STEPS ########################## //
        // 1) Prepare API response BankCode,EMIMONTHS
        // 2) Show EditText for user i/p, to filter
        // API response on the basis of BankCode,EMIMONTHS
        // 3) Read edittext on submit
        // 4) Filter the API response based on i/p in #3
        // 5) Display in the edittext below submit button
        // ################################################## //
        HashMap<String, HashMap<String, PayuEmiAmountAccordingToInterest>> emiResponse = payuResponse.getPayuEmiAmountAccordingToInterest();
        String bankCode = "";
        ArrayList<String> bankCodeEmiMonthsArray = new ArrayList<>();
        // #1
        for (Map.Entry<String, HashMap<String, PayuEmiAmountAccordingToInterest>> perBankEmiResponseIterator : emiResponse.entrySet()) {
            bankCode = perBankEmiResponseIterator.getKey();
            for (Map.Entry<String, PayuEmiAmountAccordingToInterest> emiResponseIterator : perBankEmiResponseIterator.getValue().entrySet()) {
                bankCodeEmiMonthsArray.add(bankCode + "," + emiResponseIterator.getKey());
            }
        }

        // ## This is just to make sure that the reponse user is seeing is same as dsplayed in the following dialog
        // Toast.makeText(this, "Response: " +  payuResponse.getPayuEmiAmountAccordingToInterest(), Toast.LENGTH_LONG).show();

        // Show dialog to filter the results further
        LayoutInflater layoutInflater = getLayoutInflater();
        final View viewEmiFilter = layoutInflater.inflate(R.layout.layout_emi_interest_filter, null);
        final EditText inputEditText = (EditText) viewEmiFilter.findViewById(R.id.filter_txt);
        final TextView filteredResults = (TextView) viewEmiFilter.findViewById(R.id.filtered_results);
        final TextView emiApiResponse = (TextView) viewEmiFilter.findViewById(R.id.emi_api_response);
        final Button filterSubmitButton = (Button) viewEmiFilter.findViewById(R.id.filter_submit_button);
        // Show the API response (#2)
        emiApiResponse.setText(TextUtils.join("\n", bankCodeEmiMonthsArray));
        // Show the textview where filtered results are to be shown
        filteredResults.setVisibility(View.GONE);

        Dialog dialog = new Dialog(this);
        dialog.setContentView(viewEmiFilter);
        // dialog.setTitle("Enter Bank Code and EMI months from API response above, to filter");
        dialog.show();

        // #3
        filterSubmitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide Keyboard
                InputMethodManager inputManager = (InputMethodManager) getSystemService(VerifyApiActivity.this.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(inputEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                // #3
                String[] bankCodeEmiMonths = inputEditText.getText().toString().split(",");
                String filteredResult = "No results found";

                if (bankCodeEmiMonths.length >= 2) {
                    // #4
                    PayuEmiAmountAccordingToInterest payuEmiAmountAccordingToInterest = PayuEmiAmountAccordingToInterest.payuEmiAmountAccordingToInterestResponseHelper(bankCodeEmiMonths[0], bankCodeEmiMonths[1], payuResponse.getPayuEmiAmountAccordingToInterest());
                    if (payuEmiAmountAccordingToInterest != null) {
                        filteredResult = "Response status:" + payuResponse.getResponseStatus().getStatus() + "\nemiBankInterest: " + payuEmiAmountAccordingToInterest.getEmiBankInterest() +
                                "\nbankRate: " + payuEmiAmountAccordingToInterest.getBankRate() +
                                "\nbankCharge: " + payuEmiAmountAccordingToInterest.getBankCharge() +
                                "\namount: " + payuEmiAmountAccordingToInterest.getAmount() +
                                "\ncardType: " + payuEmiAmountAccordingToInterest.getCardType() +
                                "\nemiValue: " + payuEmiAmountAccordingToInterest.getEmiValue() +
                                "\ntenure: " + payuEmiAmountAccordingToInterest.getTenure() +
                                "\nemiInterestPaid: " + payuEmiAmountAccordingToInterest.getEmiInterestPaid();
                    }
                }
                // #5
                filteredResults.setVisibility(View.VISIBLE);
                filteredResults.setText(filteredResult);
            }
        });
    }

    @Override
    public void onEligibleBinsForEMIApiResponse(PayuResponse payuResponse) {
        // now we got response, here we build an alert and show the list
        if (payuResponse.getResponseStatus().getCode() == PayuErrors.NO_ERROR) {
            LayoutInflater layoutInflater = getLayoutInflater();
            final View view = layoutInflater.inflate(R.layout.layout_eligible_bins_for_emi_response, null);
            TextView resultText = view.findViewById(R.id.tvResult);
            StringBuilder resultBuilder = new StringBuilder();
            for (EligibleEmiBins e : payuResponse.getEligibleEmiBins()) {
                resultBuilder.append("{\n bank : " + e.getBankShortName() + ",\n");
                resultBuilder.append("minAmount : " + e.getMinAmount() + ",\n");
                resultBuilder.append("isEligible : " + e.getIsEligible() + ",\n");
                ArrayList<String> cardBinsList = e.getCardBins();
                if (cardBinsList != null && cardBinsList.size() > 0) {
                    resultBuilder.append(" cardBins : [\n");
                    for (String s : cardBinsList) {
                        resultBuilder.append(s + ",");
                    }
                    resultBuilder.append("]");
                } else {
                    resultBuilder.append("cardBins : " + e.getCardBins());
                }
                resultBuilder.append("\n}");
            }

            resultText.setText(resultBuilder.toString());
            Dialog dialog = new Dialog(this);
            dialog.setContentView(view);
            // dialog.setTitle("Enter Bank Code and EMI months from API response above, to filter");
            dialog.show();
        } else {
            Toast.makeText(this, payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
        }
    }


    class UserCardsAdapter extends BaseAdapter {
        ArrayList<StoredCard> mUserCards;
        Context mContext;

        public UserCardsAdapter(Context context, ArrayList<StoredCard> userCards) {
            this.mUserCards = userCards;
            this.mContext = context;
        }

        @Override
        public int getCount() {
            if (null != mUserCards && mUserCards.size() != 0)
                return mUserCards.size();
            return 0;
        }

        @Override
        public StoredCard getItem(int position) {
            return mUserCards.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getDropDownView(int position, View view, ViewGroup parent) {
            if (view == null || !view.getTag().toString().equals("DROPDOWN")) {
                view = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
                view.setTag("DROPDOWN");
            }

            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getTitle(position));

            return view;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null || !view.getTag().toString().equals("NON_DROPDOWN")) {
                view = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
                view.setTag("NON_DROPDOWN");
            }
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getTitle(position));
            return view;
        }

        private String getTitle(int position) {
            return position >= 0 && position < mUserCards.size() ? mUserCards.get(position).getCardName() : "";
        }
    }

    class PayUNetBankingAdapter extends BaseAdapter {
        Context mContext;
        ArrayList<PaymentDetails> mNetBankingList;

        public PayUNetBankingAdapter(Context context, ArrayList<PaymentDetails> netBankingList) {
            mContext = context;
            mNetBankingList = netBankingList;
        }

        @Override
        public int getCount() {
            return mNetBankingList.size();
        }

        @Override
        public Object getItem(int i) {
            if (null != mNetBankingList) return mNetBankingList.get(i);
            else return 0;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NetbankingViewHolder netbankingViewHolder = null;
            if (convertView == null) {
                LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.netbanking_list_item, null);
                netbankingViewHolder = new NetbankingViewHolder(convertView);
                convertView.setTag(netbankingViewHolder);
            } else {
                netbankingViewHolder = (NetbankingViewHolder) convertView.getTag();
            }

            PaymentDetails paymentDetails = mNetBankingList.get(position);

            // set text here
            netbankingViewHolder.netbankingTextView.setText(paymentDetails.getBankName());
            return convertView;
        }


        class NetbankingViewHolder {
            TextView netbankingTextView;

            NetbankingViewHolder(View view) {
                netbankingTextView = (TextView) view.findViewById(R.id.text_view_netbanking);
            }
        }
    }
}