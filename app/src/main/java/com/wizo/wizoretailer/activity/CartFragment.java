package com.wizo.wizoretailer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.wizo.wizoretailer.R;
import com.wizo.wizoretailer.adapter.CartListViewAdapter;
import com.wizo.wizoretailer.barcodereader.BarcodeCaptureActivity;
import com.wizo.wizoretailer.model.Bill;
import com.wizo.wizoretailer.util.StateData;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.wizo.wizoretailer.constant.constants.RC_SCAN_BARCODE_ITEM;
import static com.wizo.wizoretailer.constant.constants.RECEIPT_ACTIVITY;


public class CartFragment extends WizoFragment {

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



    @Override
    public void onStop() {

        super.onStop();
        System.out.println("OnStop of activity fragment");
        // COMMENTED FOR WIZO RETAILER
        /*try {
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
        }*/
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






    public void handleBarcode(String barcode) {
        //Get transaction details
//        RequestParams params = new RequestParams();
//
//        params.put("trnsId", barcode);
        /*System.out.println("Sending request to fetch transaction details");
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
            });*/
        Bundle bundle = new Bundle();
        bundle.putString("TransactionId", barcode);
        bundle.putString("CallingView", "Cart");
        ((MainActivity)getActivity()).launchFragment(RECEIPT_ACTIVITY,bundle);


    }
    


    public boolean preRequisiteCheck(){
        return StateData.billAmount != 0.0f
                && StateData.storeName != null
                && StateData.transactionId != null;
    }





    @Override
    public void onBackPressed()
    {
        // Do Nothing
    }

}
