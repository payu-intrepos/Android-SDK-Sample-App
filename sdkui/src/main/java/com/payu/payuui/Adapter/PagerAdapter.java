package com.payu.payuui.Adapter;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuResponse;
import com.payu.india.Payu.PayuConstants;
import com.payu.paymentparamhelper.PaymentParams;
import com.payu.payuui.Fragment.CashCardFragment;
import com.payu.payuui.Fragment.CreditDebitFragment;
import com.payu.payuui.Fragment.EmiFragment;
import com.payu.payuui.Fragment.GenericUpiIntentFragment;
import com.payu.payuui.Fragment.LazyPayFragment;
import com.payu.payuui.Fragment.NetBankingFragment;
import com.payu.payuui.Fragment.PayuMoneyFragment;
import com.payu.payuui.Fragment.PhonePeFragment;
import com.payu.payuui.Fragment.SamsungPayFragment;
import com.payu.payuui.Fragment.SavedCardsFragment;
import com.payu.payuui.Fragment.StandAlonePhonePeFragment;
import com.payu.payuui.Fragment.TEZFragment;
import com.payu.payuui.Fragment.UPIFragment;
import com.payu.payuui.Fragment.ZestmoneyFragment;
import com.payu.payuui.SdkuiUtil.SdkUIConstants;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by piyush on 29/7/15.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<String> mTitles;
    private PayuResponse payuResponse;
    private PayuResponse valueAddedResponse;
    private HashMap<Integer, Fragment> mPageReference = new HashMap<Integer, Fragment>();
    private PaymentParams paymentParams;
    private PayuConfig payuConfig = null;
    private String salt;

    public PagerAdapter(FragmentManager fragmentManager, ArrayList<String> titles, PayuResponse payuResponse, PayuResponse valueAddedResponse, String salt) {
        super(fragmentManager);
        this.mTitles = titles;
        this.payuResponse = payuResponse;
        this.valueAddedResponse = valueAddedResponse;
        this.salt = salt;
    }

    public void setPaymentParams(PaymentParams paymentParams) {
        this.paymentParams = paymentParams;
    }

    public void setPayuConfig(PayuConfig payuConfig){
        this.payuConfig = payuConfig;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = null;
        Bundle bundle = new Bundle();
        switch (mTitles.get(i)){
            case SdkUIConstants.SAVED_CARDS :
                fragment = new SavedCardsFragment();
                bundle.putParcelableArrayList(PayuConstants.STORED_CARD, payuResponse.getStoredCards());
                bundle.putSerializable(SdkUIConstants.VALUE_ADDED, valueAddedResponse.getIssuingBankStatus());
                bundle.putInt(SdkUIConstants.POSITION, i);
                fragment.setArguments(bundle);
                mPageReference.put(i, fragment);
                return fragment;

            case SdkUIConstants.CREDIT_DEBIT_CARDS:
                fragment = new CreditDebitFragment();
                bundle.putParcelableArrayList(PayuConstants.CREDITCARD, payuResponse.getCreditCard());
                bundle.putParcelableArrayList(PayuConstants.DEBITCARD, payuResponse.getDebitCard());
                bundle.putSerializable(SdkUIConstants.VALUE_ADDED, valueAddedResponse.getIssuingBankStatus());
                bundle.putInt(SdkUIConstants.POSITION, i);
                fragment.setArguments(bundle);
                mPageReference.put(i, fragment);
                return fragment;

            case SdkUIConstants.NET_BANKING:
                fragment = new NetBankingFragment();
                bundle.putParcelableArrayList(PayuConstants.NETBANKING, payuResponse.getNetBanks());
                bundle.putParcelableArrayList(PayuConstants.SINETBANKING,payuResponse.getSiBankList());
                bundle.putSerializable(SdkUIConstants.VALUE_ADDED, valueAddedResponse.getNetBankingDownStatus());
                fragment.setArguments(bundle);
                mPageReference.put(i, fragment);
                return fragment;

            case SdkUIConstants.UPI:
                fragment = new UPIFragment();
                bundle.putParcelableArrayList(PayuConstants.NETBANKING, payuResponse.getNetBanks());
                bundle.putSerializable(SdkUIConstants.VALUE_ADDED, valueAddedResponse.getNetBankingDownStatus());
                fragment.setArguments(bundle);
                mPageReference.put(i, fragment);
                return fragment;

            case SdkUIConstants.EMI:
                fragment = new EmiFragment();
                bundle.putParcelableArrayList(PayuConstants.EMI, payuResponse.getEmi());
                bundle.putParcelableArrayList(PayuConstants.NO_COST_EMI, payuResponse.getNoCostEMI());
                fragment.setArguments(bundle);
                return fragment;

            case SdkUIConstants.CASH_CARDS:
                fragment = new CashCardFragment();
                bundle.putParcelableArrayList(PayuConstants.CASHCARD, payuResponse.getCashCard());
                fragment.setArguments(bundle);
                return  fragment;

            case SdkUIConstants.TEZ:
                fragment = new TEZFragment();
                mPageReference.put(i, fragment);
                return fragment;

            case SdkUIConstants.GENERICINTENT:
                fragment = new GenericUpiIntentFragment();
                mPageReference.put(i, fragment);
                return fragment;

            case SdkUIConstants.PAYU_MONEY:
                fragment = new PayuMoneyFragment();
                bundle.putParcelableArrayList(PayuConstants.PAYU_MONEY, payuResponse.getPaisaWallet());
                mPageReference.put(i, fragment);
                return fragment;

            case SdkUIConstants.LAZY_PAY:
                fragment = new LazyPayFragment();
                bundle.putParcelableArrayList(PayuConstants.LAZYPAY, payuResponse.getLazyPay());
                mPageReference.put(i, fragment);
                return fragment;
            case "SAMPAY":
                fragment = new SamsungPayFragment();
                mPageReference.put(i, fragment);
                return fragment;

            case SdkUIConstants.PHONEPE:
                fragment = new StandAlonePhonePeFragment();
                mPageReference.put(i,fragment);
                return fragment;

            case SdkUIConstants.CBPHONEPE:
                fragment = new PhonePeFragment();
                mPageReference.put(i,fragment);
                return fragment;

            case SdkUIConstants.ZESTMONEY:
                fragment = new ZestmoneyFragment();
                bundle.putParcelable(PayuConstants.KEY, paymentParams);
                bundle.putParcelable(PayuConstants.PAYU_CONFIG, payuConfig);
                bundle.putString(PayuConstants.SALT, salt);
                fragment.setArguments(bundle);
                return fragment;


            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        if(mTitles != null)
            return mTitles.size();
        return 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }

    public Fragment getFragment(int key){
        return mPageReference.get(key);
    }


}
