package com.payu.payuui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import com.payu.india.Extras.PayUChecksum;
import com.payu.india.Interfaces.DeleteCardApiListener;
import com.payu.india.Interfaces.DeleteCvvApiListener;
import com.payu.india.Interfaces.EditCardApiListener;
import com.payu.india.Interfaces.GetCardInformationApiListener;
import com.payu.india.Interfaces.GetIbiboCodesApiListener;
import com.payu.india.Interfaces.GetOfferStatusApiListener;
import com.payu.india.Interfaces.GetTransactionInfoApiListener;
import com.payu.india.Interfaces.GetStoredCardApiListener;
import com.payu.india.Interfaces.SaveCardApiListener;
import com.payu.india.Interfaces.ValueAddedServiceApiListener;
import com.payu.india.Interfaces.VerifyPaymentApiListener;
import com.payu.india.Model.MerchantWebService;
import com.payu.india.Model.PaymentDefaultParams;
import com.payu.india.Model.PaymentDetails;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PayuResponse;
import com.payu.india.Model.PostData;
import com.payu.india.Model.StoredCard;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.Payu.PayuUtils;
import com.payu.india.PostParams.MerchantWebServicePostParams;
import com.payu.india.Tasks.DeleteCardTask;
import com.payu.india.Tasks.DeleteCvvTask;
import com.payu.india.Tasks.EditCardTask;
import com.payu.india.Tasks.GetCardInformationTask;
import com.payu.india.Tasks.GetIbiboCodesTask;
import com.payu.india.Tasks.GetOfferStatusTask;
import com.payu.india.Tasks.GetStoredCardTask;
import com.payu.india.Tasks.GetTransactionInfoTask;
import com.payu.india.Tasks.SaveCardTask;
import com.payu.india.Tasks.ValueAddedServiceTask;
import com.payu.india.Tasks.VerifyPaymentTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PayUVerifyApiActivity extends AppCompatActivity implements View.OnClickListener, GetStoredCardApiListener,
        SaveCardApiListener, EditCardApiListener, DeleteCardApiListener, GetCardInformationApiListener,
        GetIbiboCodesApiListener, ValueAddedServiceApiListener, GetTransactionInfoApiListener, GetOfferStatusApiListener, VerifyPaymentApiListener, DeleteCvvApiListener {

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


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_verify_api, menu);
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
         if(v.getId() == R.id.button_store_user_card){
            saveUserCard();
         }else if(v.getId() == R.id.button_delete_user_card){
             deleteUserCard = true;
             editUserCard = false;
             getUserCard = false;
             deleteUserCard();
         }else if(v.getId() == R.id.button_edit_user_card){
             deleteUserCard = false;
             editUserCard = true;
             getUserCard = false;
             editUserCard();
         }else if(v.getId() == R.id.button_get_user_cards){
             deleteUserCard = false;
             editUserCard = false;
             getUserCard = true;
             getUserCard();
         }else if(v.getId() == R.id.button_get_card_information) {
             getCardInformation();
         }else if(v.getId() == R.id.button_get_ibibo_codes){
             getIbiboCodes();
         }else if(v.getId() == R.id.button_get_value_added_services) {
            getValueAddedService();
         }else if(v.getId() == R.id.button_get_transaction_information) {
            getTransactionInformation();
         }else if(v.getId() == R.id.button_get_offer_status){
             getOfferStatus();
         }else if(v.getId() == R.id.button_verify_transaction){
             verifyPayment();
         }else if(v.getId() == R.id.button_delete_cvv){
             deleteCvv();
         }
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        saveUserCardDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
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

                    SaveCardTask saveCardTask = new SaveCardTask(PayUVerifyApiActivity.this);
                    saveCardTask.execute(payuConfig);

                    // lets cancel the dialog.
                    saveUserCardDialog.dismiss();
                } else {
                    Toast.makeText(PayUVerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
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
                if (null != selectedUserCard){
                    MerchantWebService merchantWebService = new MerchantWebService();
                    merchantWebService.setKey(mPaymentParams.getKey());
                    merchantWebService.setCommand(PayuConstants.DELETE_USER_CARD);
                    merchantWebService.setVar1(mPaymentParams.getUserCredentials());
                    merchantWebService.setVar2(selectedUserCard.getCardToken());
                    merchantWebService.setHash(mPayuHashes.getDeleteCardHash());

                    postData = null;
                    postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();

                    if(postData.getCode() == PayuErrors.NO_ERROR){
                        // ok we got the post params, let make an api call to payu to fetch the payment related details

                        payuConfig.setData(postData.getResult());

                        DeleteCardTask deleteCardTask = new DeleteCardTask(PayUVerifyApiActivity.this);
                        deleteCardTask.execute(payuConfig);
                    } else {
                        Toast.makeText(PayUVerifyApiActivity.this, postData.getResult() , Toast.LENGTH_LONG).show();
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

        editUserCardDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
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

                        EditCardTask editCardTask = new EditCardTask(PayUVerifyApiActivity.this);
                        editCardTask.execute(payuConfig);

                        // lets cancel the dialog.
                        editUserCardDialog.dismiss();
                    } else {
                        Toast.makeText(PayUVerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
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

        getCardInfomationAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cardNumber;
                // validate the card number! if valid then proceed else show a toast message.
                if ((cardNumber = cardNumberEditText.getText().toString()) != null && payuUtils.validateCardNumber(cardNumber)) {

                    MerchantWebService merchantWebService = new MerchantWebService();
                    merchantWebService.setCommand(PayuConstants.CHECK_IS_DOMESTIC);
                    merchantWebService.setKey(mPaymentParams.getKey());
                    merchantWebService.setHash(mPayuHashes.getCheckIsDomesticHash());
                    // we need only the card bin. lets find the first 6 digits.
                    merchantWebService.setVar1(cardNumber.substring(0, 6));
                    MerchantWebServicePostParams merchantWebServicePostParams = new MerchantWebServicePostParams(merchantWebService);
                    postData = merchantWebServicePostParams.getMerchantWebServicePostParams();
                    if (postData.getCode() == PayuErrors.NO_ERROR) {
                        payuConfig.setData(postData.getResult());
                        GetCardInformationTask getCardInformationTask = new GetCardInformationTask(PayUVerifyApiActivity.this);
                        getCardInformationTask.execute(payuConfig);
                        getCardInfomationAlertDialog.dismiss();
                    } else {
                        Toast.makeText(PayUVerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(PayUVerifyApiActivity.this, "Invalid card number", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showValueAddedServiceResponse(PayuResponse payuResponse){
        if(null != payuResponse && (payuResponse.isIssuingBankStatusAvailable() || payuResponse.isNetBankingStatusAvailable())) {
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
        }else if (null != payuResponse && null != payuResponse.getResponseStatus()){
            Toast.makeText(PayUVerifyApiActivity.this, payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(PayUVerifyApiActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
        }
    }

    private void getIbiboCodes(){
        MerchantWebService merchantWebService = new MerchantWebService();
        merchantWebService.setKey(mPaymentParams.getKey());
        merchantWebService.setCommand(PayuConstants.GET_MERCHANT_IBIBO_CODES);
        merchantWebService.setVar1(PayuConstants.DEFAULT);
        merchantWebService.setHash(mPayuHashes.getMerchantIbiboCodesHash());

        PostData postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();
        if(postData.getCode() == PayuErrors.NO_ERROR){
            payuConfig.setData(postData.getResult());
            GetIbiboCodesTask getIbiboCodesTask = new GetIbiboCodesTask(this);
            getIbiboCodesTask.execute(payuConfig);
        }else{
            Toast.makeText(PayUVerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
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

        if((postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams()) != null&& postData.getCode() == PayuErrors.NO_ERROR ){
            payuConfig.setData(postData.getResult());
            ValueAddedServiceTask valueAddedServiceTask = new ValueAddedServiceTask(this);
            valueAddedServiceTask.execute(payuConfig);
        }else{
            Toast.makeText(this, postData.getResult(), Toast.LENGTH_LONG).show();
        }
    }

    private void getTransactionInformation(){ // TODO gotta fix, not working, ui visibility is gone now.
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

        getTransactionInformationAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
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
                        GetTransactionInfoTask getTransactionInfoTask = new GetTransactionInfoTask(PayUVerifyApiActivity.this);
                        getTransactionInfoTask.execute(payuConfig);

                        getTransactionInformationAlertDialog.dismiss();

                    } else {
                        Toast.makeText(PayUVerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(PayUVerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void getOfferStatus(){
        LayoutInflater layoutInflater = getLayoutInflater();
        View viewOfferStatus = layoutInflater.inflate(R.layout.layout_get_offer_status, null);

        final EditText offerCardNumberEditText = (EditText) viewOfferStatus.findViewById(R.id.edit_text_offer_card_number);
        final EditText offerNameOnCardEditText = (EditText) viewOfferStatus.findViewById(R.id.edit_text_offer_name_on_card);
        final EditText offerPaymentModeEditText = (EditText) viewOfferStatus.findViewById(R.id.edit_text_offer_card_mode);

        final LinearLayout newCardLinearLayout = (LinearLayout) viewOfferStatus.findViewById(R.id.linear_layout_new_card);
        final LinearLayout netBankingLinearLayout = (LinearLayout) viewOfferStatus.findViewById(R.id.linear_layout_netbanking);
        final LinearLayout storedCardLinearLayout = (LinearLayout) viewOfferStatus.findViewById(R.id.linear_layout_stored_card);


        Spinner selectOfferModeSpinner = (Spinner) viewOfferStatus.findViewById(R.id.spinner_select_offer_mode);

        final Spinner netBankingSpinner= (Spinner) viewOfferStatus.findViewById(R.id.spinner_netbanking);
        final Spinner storedCardSpinner = (Spinner) viewOfferStatus.findViewById(R.id.spinner_stored_cards);

        // lets setup offer mode spinner.

        selectOfferModeSpinner.setAdapter(ArrayAdapter.createFromResource(this, R.array.offer_modes, android.R.layout.simple_spinner_item));
        selectOfferModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0: // new card
                        newCardLinearLayout.setVisibility(View.VISIBLE);
                        storedCardLinearLayout.setVisibility(View.GONE);
                        netBankingLinearLayout.setVisibility(View.GONE);
                        break;
                    case 1: // stored card
                        newCardLinearLayout.setVisibility(View.GONE);
                        storedCardLinearLayout.setVisibility(View.VISIBLE);
                        netBankingLinearLayout.setVisibility(View.GONE);
                        break;
                    case 2: // net banking
                        newCardLinearLayout.setVisibility(View.GONE);
                        storedCardLinearLayout.setVisibility(View.GONE);
                        netBankingLinearLayout.setVisibility(View.VISIBLE);
                        break;
                    default: // new card
                        newCardLinearLayout.setVisibility(View.VISIBLE);
                        storedCardLinearLayout.setVisibility(View.GONE);
                        netBankingLinearLayout.setVisibility(View.GONE);
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

        getOfferDetailsDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Common for all the flow.
                merchantWebService = new MerchantWebService();
                merchantWebService.setKey(mPaymentParams.getKey());
                merchantWebService.setCommand(PayuConstants.CHECK_OFFER_STATUS);
                merchantWebService.setHash(mPayuHashes.getCheckOfferStatusHash());
                merchantWebService.setVar1(mPaymentParams.getOfferKey());
                merchantWebService.setVar2(mPaymentParams.getAmount());

                if(netBankingLinearLayout.getVisibility() == View.VISIBLE){ // its netbanking flow.
                    merchantWebService.setVar3("NB"); // mode
                    merchantWebService.setVar4(netBankingList.get(netBankingSpinner.getSelectedItemPosition()).getBankCode());
                }

                if(newCardLinearLayout.getVisibility() == View.VISIBLE){

                    merchantWebService.setVar3(offerPaymentModeEditText.getText().toString());
                    merchantWebService.setVar4(offerCardNumberEditText.getText().toString().startsWith("4") ? "VISA" : "MAST");
                    // Required only for new card
                    merchantWebService.setVar5(offerCardNumberEditText.getText().toString());
                    // Optional
                    merchantWebService.setVar6(offerNameOnCardEditText.getText().toString());

                }else{
                    merchantWebService.setVar5("");
                    merchantWebService.setVar6("");
                }

                merchantWebService.setVar7(mPaymentParams.getPhone());
                merchantWebService.setVar8(mPaymentParams.getEmail());
                merchantWebService.setVar9("");

                // Needed only in case of stored card
                if(storedCardLinearLayout.getVisibility() == View.VISIBLE) {
                    merchantWebService.setVar3(storedCardsList.get(storedCardSpinner.getSelectedItemPosition()).getCardMode());
                    merchantWebService.setVar4(storedCardsList.get(storedCardSpinner.getSelectedItemPosition()).getCardBin().startsWith("4") ? "VISA" : "MAST");
                    merchantWebService.setVar10(mPaymentParams.getUserCredentials());
                    merchantWebService.setVar11(storedCardsList.get(storedCardSpinner.getSelectedItemPosition()).getCardToken());
                }
                postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();

                if(postData.getCode() == PayuErrors.NO_ERROR) {
                    payuConfig.setData(postData.getResult());

                    GetOfferStatusTask getOfferStatusTask = new GetOfferStatusTask(PayUVerifyApiActivity.this);
                    getOfferStatusTask.execute(payuConfig);

                    // lets cancel the dialog.
                    getOfferDetailsDialog.dismiss();
                }else{
                    Toast.makeText(PayUVerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void verifyPayment(){
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

        verifyPaymentDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // we gotta calculate hash at our end
                PayUChecksum payUChecksum = new PayUChecksum();
                payUChecksum.setKey(mPaymentParams.getKey());
                payUChecksum.setCommand(PayuConstants.VERIFY_PAYMENT);
                // var 1 is from date
                payUChecksum.setVar1(verifyPaymentTransactionIdEditText.getText().toString());
                payUChecksum.setSalt(bundle.getString(PayuConstants.SALT));
                if((postData = payUChecksum.getHash()) != null && postData.getCode() == PayuErrors.NO_ERROR ){
                    MerchantWebService merchantWebService = new MerchantWebService();
                    merchantWebService.setKey(mPaymentParams.getKey());
                    merchantWebService.setCommand(PayuConstants.VERIFY_PAYMENT);
                    merchantWebService.setHash(postData.getResult());
                    merchantWebService.setVar1(verifyPaymentTransactionIdEditText.getText().toString());
                    if((postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams()) != null && postData.getCode() == PayuErrors.NO_ERROR ){
                        payuConfig.setData(postData.getResult());

                        // lets make api call
                        VerifyPaymentTask verifyPaymentTask = new VerifyPaymentTask(PayUVerifyApiActivity.this);
                        verifyPaymentTask.execute(payuConfig);

                        verifyPaymentDialog.dismiss();

                    }else{
                        Toast.makeText(PayUVerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(PayUVerifyApiActivity.this, postData.getResult(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void deleteCvv(){
        LayoutInflater layoutInflater = getLayoutInflater();
        View viewDeleteCardCvv = layoutInflater.inflate(R.layout.layout_delete_stored_card_cvv, null);
        ListView deleteCardCvvListView = (ListView) viewDeleteCardCvv.findViewById(R.id.list_view_delete_stored_card_cvv);

        UserCardsAdapter userCardsAdapter = new UserCardsAdapter(this, storedCardsList);
        deleteCardCvvListView.setAdapter(userCardsAdapter);

        deleteCardCvvListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedUserCard = (StoredCard) parent.getItemAtPosition(position);
            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(viewDeleteCardCvv).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (null != selectedUserCard){
                    MerchantWebService merchantWebService = new MerchantWebService();
                    merchantWebService.setKey(mPaymentParams.getKey());
                    merchantWebService.setCommand(PayuConstants.DELETE_STORE_CARD_CVV);
                    merchantWebService.setVar1(mPaymentParams.getUserCredentials());
                    merchantWebService.setVar2(selectedUserCard.getCardToken());
                    merchantWebService.setHash(mPayuHashes.getDeleteStoreCardCvv());

                    postData = null;
                    postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();

                    if(postData.getCode() == PayuErrors.NO_ERROR){
                        // ok we got the post params, let make an api call to payu to fetch the payment related details

                        payuConfig.setData(postData.getResult());

                        DeleteCvvTask deleteCvvTask = new DeleteCvvTask(PayUVerifyApiActivity.this);
                        deleteCvvTask.execute(payuConfig);
                    } else {
                        Toast.makeText(PayUVerifyApiActivity.this, postData.getResult() , Toast.LENGTH_LONG).show();
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
        if(payuResponse.isCardInformationAvailable()){
            Toast.makeText(PayUVerifyApiActivity.this, "Is Domestic: " + payuResponse.getCardInformation().getIsDomestic() + " Issuing Bank: " + payuResponse.getCardInformation().getIssuingBank() + " Card Type: " + payuResponse.getCardInformation().getCardType() + " Card Category: " + payuResponse.getCardInformation().getCardCategory() , Toast.LENGTH_LONG).show();
        }else {
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


    class UserCardsAdapter extends BaseAdapter {
        ArrayList<StoredCard> mUserCards;
        Context mContext;

        public UserCardsAdapter(Context context, ArrayList<StoredCard> userCards) {
            this.mUserCards = userCards;
            this.mContext = context;
        }

        @Override
        public int getCount() {
            if(null != mUserCards && mUserCards.size() != 0)
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
}

