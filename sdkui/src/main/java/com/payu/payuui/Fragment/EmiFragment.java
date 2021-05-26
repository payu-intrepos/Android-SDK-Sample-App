package com.payu.payuui.Fragment;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.payu.india.Model.Emi;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Payu.PayuConstants;
import com.payu.paymentparamhelper.PaymentParams;
import com.payu.payuui.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class EmiFragment extends Fragment {

    private Spinner bankNameSpinner;
    private Spinner emiDurationSpinner;
    private SpinnerAdapter emiNameAdapter;
    private SpinnerAdapter emiDurationAdapter;
    private Bundle bundle;
    private Button emiPayNowButton;
    private Emi selectedEmi;

    private EditText cardNumberEditText;
    private EditText nameOnCardEditText;
    private EditText cvvEditText;
    private EditText expiryMonthEditText;
    private EditText expiryYearEditText;

    private ArrayList<Emi> emiArrayList;
    private ArrayList<Emi> noCostEmiList;

    private PaymentParams mPaymentParams;
    private PayuHashes mPayuHashes;
    private TextView amountTextView;
    private TextView transactionIdTextView;
    private Boolean smsPermission;

    private PayuConfig payuConfig;


    public EmiFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_emi, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        bundle = getArguments();

        bankNameSpinner = (Spinner) getActivity().findViewById(R.id.spinner_emi_bank_name);
        emiDurationSpinner = (Spinner) getActivity().findViewById(R.id.spinner_emi_duration);
//        cardNumberEditText = (EditText) getActivity().findViewById(R.id.edit_text_emi_card_number);
//        nameOnCardEditText = (EditText) getActivity().findViewById(R.id.edit_text_emi_name_on_card);
//        cvvEditText = (EditText) getActivity().findViewById(R.id.edit_text_emi_cvv);
//        expiryMonthEditText = (EditText) getActivity().findViewById(R.id.edit_text_emi_expiry_month);
//        expiryYearEditText = (EditText) getActivity().findViewById(R.id.edit_text_emi_expiry_year);

        if(bundle.getParcelableArrayList(PayuConstants.NO_COST_EMI) != null){
            ArrayList<Emi> emiList = bundle.getParcelableArrayList(PayuConstants.NO_COST_EMI);
            noCostEmiList = getUniqueList(emiList);
        }

        if(bundle.getParcelableArrayList(PayuConstants.EMI) != null){
            // okay we have emi now!
            // lets setup emi name adapter.
            emiArrayList = bundle.getParcelableArrayList(PayuConstants.EMI);
            emiNameAdapter = new PayUEmiNameAdapter(getActivity(), getUniqueList(emiArrayList));
            bankNameSpinner.setAdapter(emiNameAdapter);

            bankNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Item selected, lets setup the emiDuration adapter.
                    emiDurationAdapter = new PayUEmiDurationAdapter(getActivity(), emiArrayList, (Emi) parent.getSelectedItem());
                    emiDurationSpinner.setAdapter(emiDurationAdapter);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            emiDurationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedEmi = (Emi) parent.getSelectedItem();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }else{
            Toast.makeText(getActivity(), "Could not find emil list from the privious activity", Toast.LENGTH_LONG).show();
        }


    }

    private ArrayList<Emi> getUniqueList(ArrayList<Emi> emiArrayList) {
        if (emiArrayList == null && emiArrayList.isEmpty()) return null;
        ArrayList<Emi> uniqueList = new ArrayList<>();
        HashMap<String,Integer> map = new HashMap<>();
        for (Emi e : emiArrayList){
            if (map.containsKey(e.getBankName())) continue;
            else {
                map.put(e.getBankName(), 1);
                uniqueList.add(e);
            }
        }
        return uniqueList;
    }

    class PayUEmiNameAdapter extends BaseAdapter {

        Context mContext;
        ArrayList<Emi> mEmiList;
        public PayUEmiNameAdapter(Context context, ArrayList<Emi> emiList){
            mContext = context;
            mEmiList = emiList;
        }

        @Override
        public int getCount() {
            if (null != mEmiList ) return mEmiList.size();
            else return 0;
        }

        @Override
        public Emi getItem(int position) {
            return mEmiList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PayUEmiVH emiViewHolder = null;
            if(convertView == null){
                LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.emi_list_item, null);
                emiViewHolder = new PayUEmiVH(convertView);
                convertView.setTag(emiViewHolder);
            }else{
                emiViewHolder = (PayUEmiVH) convertView.getTag();
            }

            Emi emi  = getItem(position);

            // set text here
            emiViewHolder.emiNameTextView.setText(emi.getBankName());
            if (isNoCostEmiAvailableForBank(emi.getBankName()))
                emiViewHolder.tvNoCostEmi.setVisibility(View.VISIBLE);
            else
                emiViewHolder.tvNoCostEmi.setVisibility(View.GONE);

            return convertView;
        }

        private boolean isNoCostEmiAvailableForBank(String bankName) {
            if (noCostEmiList == null || noCostEmiList.size() == 0)
                return false;

            for (Emi emi: noCostEmiList){
                if (emi.getBankName().equalsIgnoreCase(bankName))
                    return true;
            }
            return false;
        }
    }

    class PayUEmiDurationAdapter extends BaseAdapter{

        Context mContext;
        ArrayList<Emi> mEmiList;
        Emi mEmi;
        ArrayList<Emi> mSelectedEmiList;

        public PayUEmiDurationAdapter(Context context, ArrayList<Emi> emiList, Emi emi){
            mContext = context;
            mEmiList = emiList;
            mEmi = emi;
            mSelectedEmiList = null;
            mSelectedEmiList = new ArrayList<>();
            for(int i = 0; i < emiList.size(); i++){
                if(emiList.get(i).getBankName().contentEquals(emi.getBankName())){ // we found the current bank and bank is common in the list
                    mSelectedEmiList.add(emiList.get(i));
                }
            }
        }

        @Override
        public int getCount() {
            if(null != mSelectedEmiList) return mSelectedEmiList.size();
            else return 0;
        }

        @Override
        public Emi getItem(int position) {
            return mSelectedEmiList.get(position) ;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PayUEmiVH emiViewHolder = null;
            if(convertView == null){
                LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.emi_list_item, null);
                emiViewHolder = new PayUEmiVH(convertView);
                convertView.setTag(emiViewHolder);
            }else{
                emiViewHolder = (PayUEmiVH) convertView.getTag();
            }

            Emi emi  = getItem(position);
            // set text here
            emiViewHolder.emiNameTextView.setText(emi.getBankTitle());
            if (isNoCostEmiAvailbleForBankCode(emi.getBankCode())){
                emiViewHolder.tvNoCostEmi.setVisibility(View.VISIBLE);
            }else{
                emiViewHolder.tvNoCostEmi.setVisibility(View.GONE);
            }
            return convertView;
        }

        private boolean isNoCostEmiAvailbleForBankCode(String bankCode) {
            if (noCostEmiList == null || noCostEmiList.size() == 0)
                return false;

            for (Emi emi: noCostEmiList){
                if (emi.getBankCode().equalsIgnoreCase(bankCode))
                    return true;
            }
            return false;
        }
    }

    class PayUEmiVH {
        TextView emiNameTextView;
        TextView tvNoCostEmi;
        PayUEmiVH(View view) {
            emiNameTextView = (TextView) view.findViewById(R.id.text_view_emi_list);
            tvNoCostEmi = (TextView) view.findViewById(R.id.tvNoCostEmi);
        }
    }

}
