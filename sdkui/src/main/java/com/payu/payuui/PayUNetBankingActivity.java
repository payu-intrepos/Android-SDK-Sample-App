package com.payu.payuui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.payu.india.Model.PaymentDefaultParams;
import com.payu.india.Model.PaymentDetails;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.PostParams.NBPostParams;
import com.payu.india.PostParams.PaymentPostParams;

import java.util.ArrayList;


public class PayUNetBankingActivity extends AppCompatActivity implements View.OnClickListener {

    private String bankcode;
    private Bundle bundle;
    private ArrayList<PaymentDetails> netBankingList;
    private Spinner spinnerNetbanking;
    //    private String[] netBanksNamesArray;
//    private String[] netBanksCodesArray;
    private PaymentParams mPaymentParams;
    private PayuHashes payuHashes;

    private Button payNowButton;

    private PayUNetBankingAdapter payUNetBankingAdapter;
    private Toolbar toolbar;
    private PayuConfig payuConfig;

    private TextView amountTextView;
    private TextView transactionIdTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_banking);

        // todo lets set the toolbar
        /*toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);*/
        
        (payNowButton = (Button) findViewById(R.id.button_pay_now)).setOnClickListener(this);
        spinnerNetbanking = (Spinner) findViewById(R.id.spinner_netbanking);



        // lets get the required data form bundle
        bundle = getIntent().getExtras();

        if (bundle != null && bundle.getParcelableArrayList(PayuConstants.NETBANKING) != null) {
            netBankingList = new ArrayList<PaymentDetails>();
            netBankingList = bundle.getParcelableArrayList(PayuConstants.NETBANKING);

//            // initialize
//            netBanksNamesArray = new String[netBankingList.size()];
//            netBanksCodesArray = new String[netBankingList.size()];
//
//            for (int i = 0; i < netBankingList.size(); i++) {
//                netBanksNamesArray[i] = netBankingList.get(i).getBankName();
//                netBanksCodesArray[i] = netBankingList.get(i).getBankCode();
//            }

            payUNetBankingAdapter = new PayUNetBankingAdapter(this, netBankingList);
            spinnerNetbanking.setAdapter(payUNetBankingAdapter);
            spinnerNetbanking.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                    bankcode = netBankingList.get(index).getBankCode();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        } else {
            Toast.makeText(this, "Could not get netbanking list Data from the previous activity", Toast.LENGTH_LONG).show();
        }

        mPaymentParams = bundle.getParcelable(PayuConstants.PAYMENT_PARAMS);
        payuHashes = bundle.getParcelable(PayuConstants.PAYU_HASHES);
        payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);
        payuConfig = null != payuConfig ? payuConfig : new PayuConfig();

        (amountTextView = (TextView) findViewById(R.id.text_view_amount)).setText(PayuConstants.AMOUNT + ": " + mPaymentParams.getAmount());
        (transactionIdTextView = (TextView) findViewById(R.id.text_view_transaction_id)).setText(PayuConstants.TXNID + ": " + mPaymentParams.getTxnId());

    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_pay_now) {
            // okey we need hash fist
            PostData postData = new PostData();
            mPaymentParams.setHash(payuHashes.getPaymentHash());
            mPaymentParams.setBankCode(bankcode);

            postData = new PaymentPostParams(mPaymentParams, PayuConstants.NB).getPaymentPostParams();

//            postData = new NBPostParams(mPaymentParams, mNetBank).getNBPostParams();
            if (postData.getCode() == PayuErrors.NO_ERROR) {
                // launch webview
                payuConfig.setData(postData.getResult());
                Intent intent = new Intent(this, PaymentsActivity.class);
                intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
                startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
            } else {
                Toast.makeText(this, postData.getResult(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            setResult(resultCode, data);
            finish();
        }
    }

}


/*created by Guruchetan 30 jun 2015
*
* */
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
        if(null != mNetBankingList) return mNetBankingList.get(i);
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
