package com.wizo.wizoretailer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.wizo.wizoretailer.R;
import com.wizo.wizoretailer.adapter.CartListViewAdapter;
import com.wizo.wizoretailer.barcodereader.BarcodeCaptureActivity;
import com.wizo.wizoretailer.model.Bill;
import com.wizo.wizoretailer.model.CartItem;
import com.wizo.wizoretailer.model.Product;
import com.wizo.wizoretailer.model.Transaction;
import com.wizo.wizoretailer.util.CommonUtils;
import com.wizo.wizoretailer.util.Currency;
import com.wizo.wizoretailer.util.SharedPreferrencesUtil;
import com.wizo.wizoretailer.util.StateData;
import com.wizo.wizoretailer.util.TransactionStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.wizo.wizoretailer.R.drawable.app_icon;
import static com.wizo.wizoretailer.constant.constants.RC_SCAN_BARCODE_ITEM;
import static com.wizo.wizoretailer.constant.constants.SP_TRANSACTION_ID;
import static com.wizo.wizoretailer.constant.constants.SP_TRANSACTION_STATUS;
import static com.wizo.wizoretailer.constant.constants.SP_TRANSACTION_UPDATED_TS;
import static com.wizo.wizoretailer.constant.constants.TRANSACTION_CREATE_EP;
import static com.wizo.wizoretailer.constant.constants.TRANSACTION_UPDATE_EP;
import static com.wizo.wizoretailer.constant.constants.TRANSACTION_URL;


public class CartFragment extends WizoFragment implements PaymentResultListener {

    private TextView mTextMessage;
    private ListView cartListView;
    //Store details

    private Bill bill = null;

    //Floating action buttons
    private Button scanReceipt;
    private Button payButton;
    //private TextView itemCount;
    private AsyncHttpClient ahttpClient = new AsyncHttpClient();
    private CartListViewAdapter cartAdapter;

    private int emulatorCounter = 0;
    private String TAG = "CartFragment";
   private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (view != null) {
            return view;
        }
        view = inflater.inflate(R.layout.activity_cart,container,false);

        // set pay button listener
//        payButton = view.findViewById(R.id.payButton);
        //itemCount =  view.findViewById(R.id.itemCount);
//        payButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try
//                {
//                    persistTransactionData(true, TransactionStatus.CHECKOUT);
//                }
//                catch (Exception e)
//                {
//                    // TODO: handle
//                }
//            }
//        });
//        payButton.setVisibility(View.INVISIBLE)

        // set store details
        ((TextView) view.findViewById(R.id.storeTitle)).setText(StateData.store.getTitle());

//        // transaction details
//        boolean newTransaction = false;
//       // continue a transaction
//        if(StateData.transactionId != null)
//        {
//            Log.d(TAG, "Retrieve Existing transaction " + StateData.transactionId );
//
//        }
//        // first time activity is created
//        else
//        {
//            newTransaction =  true;
//            Log.d(TAG, "creating transcation for first time"   );
//
//            try {
//                persistTransactionData(false,TransactionStatus.INITIATED);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//        if(!newTransaction) {
//
//            RequestParams params = new RequestParams();
//            params.put("trnsId", StateData.transactionId);
//
//            ahttpClient.get(TRANSACTION_URL, params, new JsonHttpResponseHandler() {
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//
//                    Log.d(TAG, "Retrieved Cart " + response.toString() );
//                    Type listType = new TypeToken<ArrayList<CartItem>>(){}.getType();
//                    List<CartItem> cartList = null;
//                    try {
//                        cartList = new Gson().fromJson(response.getJSONArray("cart").toString(), listType);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        initializeCartAdpater(null);
//                        return;
//                    }
//                   initializeCartAdpater(cartList);
//                }
//            });
//
//        }
//        else{
//           initializeCartAdpater(null);
//        }
        //Initialize the scan button and its clickListener
        scanReceipt = (Button) view.findViewById(R.id.scanReceipt);
        scanReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               launchBarcodeScanner();

            }
        });

        return view;

    }

    private void initializeCartAdpater( List<CartItem> cartList )
    {
        if(cartList == null)
        cartAdapter = new CartListViewAdapter(getActivity());
        else
            cartAdapter = new CartListViewAdapter(getActivity(),cartList);
        //Link the cartList and the adapter
//        cartListView =  view.findViewById(R.id.cartList);
//        cartListView.setAdapter(cartAdapter);
//        cartAdapter.registerDataSetObserver(new DataSetObserver() {
//            @Override
//            public void onChanged() {
//                //updateAndShowBill();
//            }
//        });
//        //Set swipe to delete functionlaity
//        setSwipeDelItem();
    }

    @Override
    public void onStop() {

        super.onStop();
        System.out.println("OnStop of activity fragment");
        try {
            if(StateData.status != TransactionStatus.PAYMENT_SUCCESSFUL){
                persistTransactionData(false,TransactionStatus.SUSPENDED);
                // if the cart is empty dont remember this transaction
                if(cartAdapter != null && cartAdapter.getCartItemList() != null && cartAdapter.getCount() > 0)
                {
                    SharedPreferrencesUtil.setStringPreference(getActivity(),SP_TRANSACTION_ID,StateData.transactionId);
                    SharedPreferrencesUtil.setDatePreference(getActivity(),SP_TRANSACTION_UPDATED_TS, CommonUtils.getCurrentDate());
                    SharedPreferrencesUtil.setStringPreference(getActivity(),SP_TRANSACTION_STATUS, TransactionStatus.SUSPENDED.name());

                }
                else
                {
                    SharedPreferrencesUtil.setStringPreference(getActivity(),SP_TRANSACTION_ID,null);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle bundle = data.getExtras();
        switch (requestCode) {
            case RC_SCAN_BARCODE_ITEM:
                if (resultCode == RESULT_OK) {
                    String barcode = bundle.getString("Barcode");
                    if(barcode != null) {
                        System.out.println("=====> Control returned from Scan Barcode Activity. Barcode : " + barcode);
                        // TODO: Change it to backend later
                        //populateDummyScanProd();
                        handleBarcode(barcode);
                    }
                }
                else if(resultCode == RESULT_CANCELED )
                {
                    String reason = bundle.getString("Reason");
                    if(reason != null && reason.equalsIgnoreCase("Timeout"))
                        Toast.makeText(getActivity(),getResources().getString(R.string.toast_scan_timedout),Toast.LENGTH_LONG).show();
                }
                break;
        }
    }



    public void calculateBill() {
        if(this.bill == null){
            this.bill = new Bill(cartAdapter.getTotalAmount(), cartAdapter.getTotalSavings(), 0, Currency.INR);
            this.bill.notifyChanges();
        }else {
            this.bill.setSubTotal(cartAdapter.getTotalAmount());
            this.bill.setSavings(cartAdapter.getTotalSavings());
            this.bill.setTotalWeight(cartAdapter.getTotalWeight());
            this.bill.notifyChanges();
        }
    }

    /*public void updateAndShowBill(){
        calculateBill();
        payButton.setText(getResources().getString(R.string.pay_button)+bill.getTotal());
        itemCount.setText(String.valueOf(cartAdapter.getItemCount()));
        if(cartAdapter.getItemCount() == 0)
        {
            payButton.setVisibility(View.INVISIBLE);
        }
        else
            if(payButton.getVisibility() == View.INVISIBLE)
        {
            payButton.setVisibility(View.VISIBLE);
        }

    }*/

    public void launchBarcodeScanner() {
        emulatorCounter++;
        System.out.println("In launchBarcodeScanner");
        //Launch the bar scanner activity
        Intent barcodeScanIntent = new Intent(getActivity(),BarcodeCaptureActivity.class);
        barcodeScanIntent.putExtra("requestCode",RC_SCAN_BARCODE_ITEM);
        startActivityForResult(barcodeScanIntent,RC_SCAN_BARCODE_ITEM);

        //Bypassing scan activity to directly hit the service and get dummy data. Should remove this portion in actual app
       // populateDummyScanProd();
    }

    public JSONArray getCart() throws JSONException {
        JSONObject cartDetailsJson = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        Log.d(TAG, "getcart " );

        if(cartAdapter == null || cartAdapter.getCartItemList() == null || cartAdapter.getCartItemList().isEmpty())
            return null;


        for(CartItem item : cartAdapter.getCartItemList()) {

            Product product = item.getProduct();
            JSONObject productObj = new JSONObject();
            productObj.put("uniqueId",product.getUniqueId());
            productObj.put("barcode",product.getBarcode());
            productObj.put("title",product.getTitle());
            productObj.put("description",product.getDescription());
            productObj.put("category",product.getCategory());
            productObj.put("retailPrice",product.getRetailPrice());
            productObj.put("discount",product.getDiscount());

            JSONObject cartObj = new JSONObject();
            cartObj.put("product",productObj);
            cartObj.put("quantity",item.getQuantity());

            jsonArray.put(cartObj);
        }

        return jsonArray;

    }

    public void persistTransactionData(final boolean launchPayment,final TransactionStatus status) throws JSONException {
        String currentTS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date());
        //Store object preparation

        JSONObject store = new JSONObject();
        store.put("id", StateData.storeId);
        JSONObject jBill = null;
        if(bill != null) {
            //Bill object preparation
            jBill = new JSONObject();
            jBill.put("subTotal", bill.getSubTotal());
            jBill.put("tax", bill.getTax());
            jBill.put("currency", bill.getCurrency().toString());
            jBill.put("total", bill.getTotal());
            jBill.put("savings", bill.getSavings());
            jBill.put("totalWeight",bill.getTotalWeight());
            StateData.billAmount = bill.getTotal();

        }
        JSONArray cart = getCart();
        JSONObject jUser = new JSONObject();
        jUser.put("userId",StateData.userId);

        JSONObject trnsReq = new JSONObject();
        trnsReq.put("trnsDate", currentTS);
        trnsReq.put("status", status);
        trnsReq.put("updateTS", currentTS);
        trnsReq.put("store", store);
        trnsReq.put("cart",cart);
        trnsReq.put("bill", jBill);
        trnsReq.put("customer",jUser);
        if (StateData.transactionId == null) {

            trnsReq.put("createTS", currentTS);
            // Invoking create transaction
            StringEntity requestEntity = new StringEntity(trnsReq.toString(), ContentType.APPLICATION_JSON);
            Log.d(TAG, "Invoking create transaction. Request : " + trnsReq.toString());
            persistTransaction(launchPayment,requestEntity,TRANSACTION_CREATE_EP,status);

        } else {

            trnsReq.put("trnsId", StateData.transactionId);
            HttpEntity requestEntity = new StringEntity(trnsReq.toString(), ContentType.APPLICATION_JSON);
            Log.d(TAG, "Update transaction status triggered. " + trnsReq.toString());
            persistTransaction(launchPayment,requestEntity,TRANSACTION_UPDATE_EP,status);
        }
    }

    private void persistTransaction(final boolean launchPayment,HttpEntity requestEntity,String httpURL,final TransactionStatus status )
    {
        ahttpClient.post(getActivity(), httpURL, requestEntity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    //Setting transaction id into state data
                    Log.d(TAG, "Update Transaction Successful");
                    StateData.transactionId = response.getString("trnsId");
                    Log.d(TAG, "Updated transaction id : " + StateData.transactionId);

                    if(launchPayment) {
                        // Set state data for transaction receipt
                        updateStateTransactionReceipt(status);
                        if(preRequisiteCheck())
                            launchRazorPay(getActivity());
                    }
                } catch (Exception e) {
                    // TODO: throw custom exception
                }
            }
        });
    }

    private void updateStateTransactionReceipt(TransactionStatus status)
    {
        StateData.transactionReceipt = new Transaction();
        StateData.transactionReceipt.setTrnsId(StateData.transactionId);
        StateData.transactionReceipt.setTrnsDate(new Date().getTime());
        StateData.transactionReceipt.setStatus(status.name());
        StateData.transactionReceipt.setStore(StateData.store);
        StateData.transactionReceipt.setBill(bill);
        StateData.transactionReceipt.setCart(cartAdapter.getCartItemList());
    }

    public void handleBarcode(String barcode) {
        //Get transaction details
        RequestParams params = new RequestParams();

        params.put("trnsId", barcode);
        System.out.println("Sending request to fetch transaction details");
        //progressBar.setVisibility(View.VISIBLE);

        ahttpClient.get(TRANSACTION_URL, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    Log.d(TAG, "Retrieved Cart " + response.toString() );
                    Type listType = new TypeToken<ArrayList<CartItem>>(){}.getType();
                    List<CartItem> cartList = null;
                    try {
                        Gson gson = new Gson();
                        cartList = gson.fromJson(response.getJSONArray("cart").toString(), listType);
                        StateData.transactionReceipt = new Transaction();
                        StateData.transactionReceipt.setTrnsId(response.getString("trnsId"));
                        StateData.transactionReceipt.setTrnsDate(response.getLong("trnsDate"));
                        StateData.transactionReceipt.setStatus(response.getString("status"));
                        StateData.transactionReceipt.setStore(StateData.store);
                        JSONObject billJSON = response.getJSONObject("bill");
                        Bill bill = new Bill(billJSON.getDouble("subTotal"), billJSON.getDouble("savings"), (float)0.0, Currency.INR);
                        bill.setTotal(billJSON.getDouble("total"));
                        bill.setTotal(billJSON.getDouble("totalWeight"));
                        StateData.transactionReceipt.setBill(bill);
                        StateData.transactionReceipt.setCart(cartList);
                        System.out.println("Transaction retreived");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Snackbar.make(view, "Valid receipt. An error occured in fetching transaction details", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                System.out.println("Failed to find transaction");
                Snackbar.make(view, "Invalid receipt.", Snackbar.LENGTH_SHORT).show();
            }
            });

    }
    


    public boolean preRequisiteCheck(){
        return StateData.billAmount != 0.0f
                && StateData.storeName != null
                && StateData.transactionId != null;
    }

    public void launchRazorPay(Activity srcActivity){
        try{
            //Razor pay checkout object preparation
            Checkout checkout = new Checkout();
            JSONObject updateTransReq = new JSONObject();

            Float amount = StateData.billAmount *100;
            checkout.setImage(app_icon);
            checkout.setKeyID("rzp_test_wnre6SUsbTyIJO");
            checkout.setFullScreenDisable(true);

            // Razor pay options
            JSONObject options = new JSONObject();
            options.put("key", "rzp_test_wnre6SUsbTyIJO");
            options.put("name",StateData.storeName);
            //options.put("description", StateData.transactionId);
            options.put("amount", amount.intValue());
            options.put("currency", "INR");

            // Update transaction status
            updateTransReq.put("trnsId", StateData.transactionId);
            updateTransReq.put("status", TransactionStatus.PAYMENT_INITIATED);
            persistTransaction(false,new StringEntity(updateTransReq.toString(), ContentType.APPLICATION_JSON),TRANSACTION_UPDATE_EP,TransactionStatus.PAYMENT_INITIATED);
            Log.d(TAG,"Update transaction status triggered. " + updateTransReq.toString());

            // Trigger Razor Pay
            checkout.open(srcActivity, options);
            Log.d(TAG,"Payment request initiated");


        }catch(JSONException je){
            Log.e(TAG, je.getMessage());
        }
        catch(Exception e){
            Log.e(TAG, ": " + e.getMessage());
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        Log.d(TAG, "Payment successful. Gateway payment ref : " + razorpayPaymentID);

//        Toast.makeText(getActivity(), "Payment Successful", Toast.LENGTH_SHORT).show();
//        Log.d(TAG, "Payment successful. Gateway payment ref : " + razorpayPaymentID);
//

    }

    @Override
    public void onPaymentError(int code, String response) {

//        switch (code)
//        {
//            case Checkout.NETWORK_ERROR:
//                Log.i("tag","Network error from Razor Pay");
//                Toast.makeText(getApplicationContext(),getResources().getString(R.string.toast_not_connected_internet),Toast.LENGTH_SHORT).show();
//                break;
//            case Checkout.INVALID_OPTIONS:
//                Log.i("tag","Invalid Options from Razor Pay");
//                Toast.makeText(getApplicationContext(),getResources().getString(R.string.toast_payment_exited),Toast.LENGTH_SHORT).show();
//                break;
//            case Activity.RESULT_CANCELED:
//                Log.i("tag","Payment cancelled from Razor Pay");
//                Toast.makeText(getApplicationContext(),getResources().getString(R.string.toast_payment_exited),Toast.LENGTH_SHORT).show();
//                break;
//        }
//
//        JSONObject updateTransReq = new JSONObject();
//
//        try {
//            JSONObject payment = new JSONObject();
//            updateTransReq.put("trnsId", StateData.transactionId);
//            updateTransReq.put("status", TransactionStatus.PAYMENT_FAILURE);
//            updateTransReq.put("payment", new JSONArray().put(payment));
//            Log.d(TAG,"Update transaction status triggered. " + updateTransReq.toString());
//
//            updateTransaction(new StringEntity(updateTransReq.toString(), ContentType.APPLICATION_JSON));
//
//        }catch(Exception e){
//            //Todo
//        }
//        Log.e(TAG,response);
//
//        launchCartActivity();
    }

    @Override
    public void onBackPressed()
    {
        // Do Nothing
    }

}
