package com.payu.payuui.Fragment;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.payu.india.Model.PaymentDetails;
import com.payu.india.Payu.PayuConstants;
import com.payu.payuui.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class CashCardFragment extends Fragment {
    private ArrayList<PaymentDetails> walletList;
    private Spinner spinnerWallets;
    private ArrayAdapter<String> mAdapter;
    private View view;
    public CashCardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        walletList = getArguments().getParcelableArrayList(PayuConstants.CASHCARD);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_cash_card, container, false);
        if(null!=walletList && !walletList.isEmpty()) {
            List<String> spinnerArray = new ArrayList<String>();
            for(PaymentDetails paymentDetails:walletList){
                spinnerArray.add(paymentDetails.getBankName());
            }
            spinnerWallets = (Spinner) view.findViewById(R.id.spinnerWallets);
            mAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, spinnerArray);
            mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerWallets.setAdapter(mAdapter);
            spinnerWallets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
//                    bankcode = walletList.get(index).getBankCode();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
        return view;
    }
}
