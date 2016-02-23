package com.payu.payuui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.payu.india.Interfaces.DeleteCardApiListener;
import com.payu.india.Interfaces.GetStoredCardApiListener;
import com.payu.india.Model.MerchantWebService;
import com.payu.india.Model.PaymentDefaultParams;
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
import com.payu.india.PostParams.PaymentPostParams;
import com.payu.india.PostParams.StoredCardPostParams;
import com.payu.india.Tasks.DeleteCardTask;
import com.payu.india.Tasks.GetStoredCardTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class PayUOneClickPaymentActivity extends AppCompatActivity implements GetStoredCardApiListener, DeleteCardApiListener {

    private ListView storedCardListView;
    private PayUStoredCardsAdapter payUStoredCardsAdapter;
    private Bundle bundle;
    private ArrayList<StoredCard> storedCardList;

    private PayuHashes payuHashes;
    private PaymentParams mPaymentParams;
    private Toolbar toolbar;
    private TextView amountTextView;
    private TextView transactionIdTextView;

    private PayuConfig payuConfig;
    private PayuUtils payuUtils;

    private HashMap<String, String>oneClickCardTokens;
    private int storeOneClickHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_cards);

        // TODO lets set the toolbar
        /*toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);*/

        storedCardListView = (ListView) findViewById(R.id.list_view_user_card);

        // lets get the required data form bundle
        bundle = getIntent().getExtras();
        payuUtils = new PayuUtils();
        storeOneClickHash = bundle.getInt(PayuConstants.STORE_ONE_CLICK_HASH);

        if (bundle != null && bundle.getParcelableArrayList(PayuConstants.STORED_CARD) != null) {
            storedCardList = new ArrayList<StoredCard>();
            storedCardList = bundle.getParcelableArrayList(PayuConstants.STORED_CARD);
            payUStoredCardsAdapter = new PayUStoredCardsAdapter(this, storedCardList);
            storedCardListView.setAdapter(payUStoredCardsAdapter);
        } else {
            // we gotta fetch data from server
            Toast.makeText(this, "Could not get user card list from the previous activity", Toast.LENGTH_LONG).show();
        }

        payuHashes = bundle.getParcelable(PayuConstants.PAYU_HASHES);
        mPaymentParams = bundle.getParcelable(PayuConstants.PAYMENT_PARAMS);
        payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);

        oneClickCardTokens = (HashMap<String, String>) bundle.getSerializable(PayuConstants.ONE_CLICK_CARD_TOKENS);

        payuConfig = null != payuConfig ? payuConfig : new PayuConfig();

        amountTextView = (TextView) findViewById(R.id.text_view_amount);
        transactionIdTextView = (TextView) findViewById(R.id.text_view_transaction_id);

        amountTextView.setText(PayuConstants.AMOUNT + ": " + mPaymentParams.getAmount());
        transactionIdTextView.setText(PayuConstants.TXNID + ": " + mPaymentParams.getTxnId());

        // one click payment:
        // if there is only one stored card Make the payment directly: (Like to surprise the users. :) )

        if (null != storedCardList && storedCardList.size() == 1 && (!payuUtils.getFromSharedPreferences(PayUOneClickPaymentActivity.this, storedCardList.get(0).getCardToken()).contains(PayuConstants.DEFAULT) || null != oneClickCardTokens.get(storedCardList.get(0).getCardToken()) )) { // yeay we can make payment
            makePayment(storedCardList.get(0));
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_cards, menu);
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
    public void onGetStoredCardApiResponse(PayuResponse payuResponse) {
        Toast.makeText(this, payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
        payUStoredCardsAdapter = null;
//        payUStoredCardsAdapter = new PayUStoredCardsAdapter(this, storedCardList=payuResponse.getStoredCards());
        storedCardList = null;
        HashMap<String, ArrayList<StoredCard>> storedCardMap = new HashMap<>();
        switch (storeOneClickHash){
            case PayuConstants.STORE_ONE_CLICK_HASH_MOBILE:
                storedCardMap = new PayuUtils().getStoredCard(this, payuResponse.getStoredCards());
                storedCardList = storedCardMap.get(PayuConstants.ONE_CLICK_CHECKOUT);
                break;
            case PayuConstants.STORE_ONE_CLICK_HASH_SERVER:
                storedCardMap = new PayuUtils().getStoredCard(payuResponse.getStoredCards(), oneClickCardTokens);
                storedCardList = storedCardMap.get(PayuConstants.ONE_CLICK_CHECKOUT);
                break;
            case PayuConstants.STORE_ONE_CLICK_HASH_NONE: // all are stored cards.
            default:
                storeOneClickHash = 0;
                storedCardList = payuResponse.getStoredCards();
                break;
        }

        payUStoredCardsAdapter = new PayUStoredCardsAdapter(this, storedCardList);
        storedCardListView.setAdapter(payUStoredCardsAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            setResult(resultCode, data);
            finish();
        }
    }


    //Adaptor
    public class PayUStoredCardsAdapter extends BaseAdapter { // todo rename to storedcardAdapter

        private ArrayList<StoredCard> mStoredCards;
        private Context mContext;

        public PayUStoredCardsAdapter(Context context, ArrayList<StoredCard> StoredCards) {
            mContext = context;
            mStoredCards = StoredCards;
        }

        private void viewHolder(ViewHolder holder, int position) {
//            holder.setPosition(position);
            String issuer = payuUtils.getIssuer(mStoredCards.get(position).getCardBin());
            switch (issuer) {
                case PayuConstants.VISA:
                    holder.cardIconImageView.setImageResource(R.mipmap.visa);
                    break;
                case PayuConstants.LASER:
                    holder.cardIconImageView.setImageResource(R.mipmap.laser);
                    break;
                case PayuConstants.DISCOVER:
                    holder.cardIconImageView.setImageResource(R.mipmap.discover);
                    break;
                case PayuConstants.MAES:
                    holder.cardIconImageView.setImageResource(R.mipmap.maestro);
                    break;
                case PayuConstants.MAST:
                    holder.cardIconImageView.setImageResource(R.mipmap.master);
                    break;
                case PayuConstants.AMEX:
                    holder.cardIconImageView.setImageResource(R.mipmap.amex);
                    break;
                case PayuConstants.DINR:
                    holder.cardIconImageView.setImageResource(R.mipmap.diner);
                    break;
                case PayuConstants.JCB:
                    holder.cardIconImageView.setImageResource(R.mipmap.jcb);
                    break;
                case PayuConstants.SMAE:
                    holder.cardIconImageView.setImageResource(R.mipmap.maestro);
                    break;
                default:
                    holder.cardIconImageView.setImageResource(R.mipmap.card);
                    break;

            }

            holder.cardNumberTextView.setText(mStoredCards.get(position).getMaskedCardNumber());
            holder.cardNameTextView.setText(mStoredCards.get(position).getCardName());

            // one click payment
            holder.cvvEditText.setVisibility(View.GONE);
            holder.paynNowButton.setEnabled(true);

        }

        @Override
        public int getCount() {
            if(mStoredCards != null)
                return mStoredCards.size();
            else
                return 0;
        }

        @Override
        public Object getItem(int index) {
            if(null != mStoredCards) return mStoredCards.get(index);
            else return 0;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.user_card_item, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.setPosition(position);
            viewHolder(holder, position);

            return convertView;
        }


        class ViewHolder implements View.OnClickListener {

            int position; //for index

            ImageView cardIconImageView;
            ImageView cardTrashImageView;
            TextView cardNumberTextView;
            TextView cardNameTextView;
            LinearLayout cvvPayNowLinearLayout;
            LinearLayout rowLinearLayout;
            Button paynNowButton;
            EditText cvvEditText;
            CheckBox storeCvvCheckBox;

            public void setPosition(int position) {
                this.position = position;
            }

            public ViewHolder(View itemView) {

                cardIconImageView = (ImageView) itemView.findViewById(R.id.image_view_card_icon);
                cardNumberTextView = (TextView) itemView.findViewById(R.id.text_view_card_number);
                cardTrashImageView = (ImageView) itemView.findViewById(R.id.image_view_card_trash);
                cardNameTextView = (TextView) itemView.findViewById(R.id.text_view_card_name);
                rowLinearLayout = (LinearLayout) itemView.findViewById(R.id.linear_layout_row);
                cvvPayNowLinearLayout = (LinearLayout) itemView.findViewById(R.id.linear_layout_cvv_paynow);
                paynNowButton = (Button) itemView.findViewById(R.id.button_pay_now);
                cvvEditText = (EditText) itemView.findViewById(R.id.edit_text_cvv);

                // lets restrict the user not from typing alpha characters.


//                cvvPayNowLinearLayout.setOnClickListener(this);
//                rowLinearLayout.setOnClickListener(this);
                paynNowButton.setOnClickListener(this);
                cardTrashImageView.setOnClickListener(this);


            }

            @Override
            public void onClick(View view) {
                if (cvvPayNowLinearLayout.getVisibility() == View.VISIBLE) {
                    cvvPayNowLinearLayout.setVisibility(View.GONE);
                } else {
                    cvvPayNowLinearLayout.setVisibility(View.VISIBLE);
                }
                if (view.getId() == R.id.image_view_card_trash) {
                    deleteCard(storedCardList.get(position));
                } else if (view.getId() == R.id.button_pay_now) {
                    makePayment(storedCardList.get(position));
//                    makePayment(storedCardList.get(position), cvvEditText.getText().toString(), storeCvvCheckBox.isChecked());
                }
            }
        }
    }

    private void makePayment(StoredCard storedCard) {
        PostData postData = new PostData();
        // lets try to get the post params
        postData = null;

        mPaymentParams.setHash(payuHashes.getPaymentHash()); // make sure that you set payment hash
        mPaymentParams.setCardToken(storedCard.getCardToken());

        mPaymentParams.setNameOnCard(storedCard.getNameOnCard());
        mPaymentParams.setCardName(storedCard.getCardName());
        mPaymentParams.setExpiryMonth(storedCard.getExpiryMonth());
        mPaymentParams.setExpiryYear(storedCard.getExpiryYear());

        String merchantHash;
        if(storeOneClickHash == PayuConstants.STORE_ONE_CLICK_HASH_SERVER)
            merchantHash = oneClickCardTokens.get(storedCard.getCardToken());
        else
            merchantHash = payuUtils.getFromSharedPreferences(PayUOneClickPaymentActivity.this, storedCard.getCardToken());
//        String merchantHash = payuUtils.getFromSharedPreferences(PayUOneClickPaymentActivity.this, storedCard.getCardToken());

        if(null != merchantHash)
            mPaymentParams.setCardCvvMerchant(merchantHash);

        postData = new PaymentPostParams(mPaymentParams, PayuConstants.CC).getPaymentPostParams();

        if (postData.getCode() == PayuErrors.NO_ERROR) {
            payuConfig.setData(postData.getResult());
            Intent intent = new Intent(this, PaymentsActivity.class);
            intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
            intent.putExtra(PayuConstants.STORE_ONE_CLICK_HASH, storeOneClickHash);
            startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
        } else {
            Toast.makeText(this, postData.getResult(), Toast.LENGTH_SHORT).show();
        }

    }

    private void deleteCard(StoredCard storedCard) {
        MerchantWebService merchantWebService = new MerchantWebService();
        merchantWebService.setKey(mPaymentParams.getKey());
        merchantWebService.setCommand(PayuConstants.DELETE_USER_CARD);
        merchantWebService.setVar1(mPaymentParams.getUserCredentials());
        merchantWebService.setVar2(storedCard.getCardToken());
        merchantWebService.setHash(payuHashes.getDeleteCardHash());

        PostData postData = null;
        postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();

        if (postData.getCode() == PayuErrors.NO_ERROR) {
            // ok we got the post params, let make an api call to payu to fetch
            // the payment related details
            payuConfig.setData(postData.getResult());
            payuConfig.setEnvironment(payuConfig.getEnvironment());

            DeleteCardTask deleteCardTask = new DeleteCardTask(this);
            deleteCardTask.execute(payuConfig);
        } else {
            Toast.makeText(this, postData.getResult(), Toast.LENGTH_LONG).show();
        }

        // lets implement delete merchant hash api

//        final String postParams = "card_token=" + storedCard.getCardToken();
//
//        new AsyncTask<Void, Void, Void>() {
//
//            @Override
//            protected Void doInBackground(Void... params) {
//                try {
//                    //  https://mobiled ev.payu.in/admin/wis.php?action=add&uid=124&mid=457&token=74588&cvvhash=0123456789031
//
//                    URL url = new URL("https://payu.herokuapp.com/delete_merchant_hash");
//
//                    byte[] postParamsByte = postParams.getBytes("UTF-8");
//
//                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                    conn.setRequestMethod("POST");
//                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//                    conn.setRequestProperty("Content-Length", String.valueOf(postParamsByte.length));
//                    conn.setDoOutput(true);
//                    conn.getOutputStream().write(postParamsByte);
//
//                    InputStream responseInputStream = conn.getInputStream();
//                    StringBuffer responseStringBuffer = new StringBuffer();
//                    byte[] byteContainer = new byte[1024];
//                    for (int i; (i = responseInputStream.read(byteContainer)) != -1; ) {
//                        responseStringBuffer.append(new String(byteContainer, 0, i));
//                    }
//
//                    JSONObject response = new JSONObject(responseStringBuffer.toString());
//
//                    // pass these to next activity
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                } catch (ProtocolException e) {
//                    e.printStackTrace();
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                super.onPostExecute(aVoid);
//                this.cancel(true);
//            }
//        }.execute();
    }

    @Override
    public void onDeleteCardApiResponse(PayuResponse payuResponse) {
        if (payuResponse.isResponseAvailable()) {
            Toast.makeText(this, payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
        }
        if (payuResponse.getResponseStatus().getCode() == PayuErrors.NO_ERROR) {
            // there is no error, lets fetch te cards list.

            MerchantWebService merchantWebService = new MerchantWebService();
            merchantWebService.setKey(mPaymentParams.getKey());
            merchantWebService.setCommand(PayuConstants.GET_USER_CARDS);
            merchantWebService.setVar1(mPaymentParams.getUserCredentials());
            merchantWebService.setHash(payuHashes.getStoredCardsHash());

            PostData postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();

            if (postData.getCode() == PayuErrors.NO_ERROR) {
                // ok we got the post params, let make an api call to payu to fetch the payment related details

                payuConfig.setData(postData.getResult());
                payuConfig.setEnvironment(payuConfig.getEnvironment());

                GetStoredCardTask getStoredCardTask = new GetStoredCardTask(this);
                getStoredCardTask.execute(payuConfig);
            } else {
                Toast.makeText(this, postData.getResult(), Toast.LENGTH_LONG).show();
            }

        }
    }

}


