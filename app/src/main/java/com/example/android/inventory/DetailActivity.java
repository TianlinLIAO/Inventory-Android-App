package com.example.android.inventory;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract;

/**
 * Created by Tianlin Liao on 4/4/2017.
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Identifier for the product data loader
    private static final int EXISTING_PRODUCT_LOADER = 0;

    //Content URI for the existing product
    private Uri mCurrentProductUri;

    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierEditText;
    private EditText mSupplierEmailEditText;
    private EditText mSupplierTelEditText;

    private Button mIncreaseButton;
    private Button mDecreaseButton;
    private ImageButton mOrderViaEmailButton;
    private ImageButton mOrderViaPhoneButton;

    private Button mLoadImageButton;
    private ImageView mImageView;
    private Uri mImageUri;

    private boolean mProductEdited = false;

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int PICK_IMAGE_REQUEST = 0;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mProductEdited = true;
            return false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //Find all relevant views that we'll need user input
        mNameEditText = (EditText) findViewById(R.id.name_edit_text);
        mPriceEditText = (EditText) findViewById(R.id.price_edit_text);
        mQuantityEditText = (EditText) findViewById(R.id.quantity_edit_text);
        mSupplierEditText = (EditText) findViewById(R.id.supplier_name_edit_text);
        mSupplierEmailEditText = (EditText) findViewById(R.id.supplier_email_edit_text);
        mSupplierTelEditText = (EditText) findViewById(R.id.supplier_tel_edit_text);

        //Setup OnTouchListeners on all the input fields and buttons
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mSupplierEmailEditText.setOnTouchListener(mTouchListener);
        mSupplierTelEditText.setOnTouchListener(mTouchListener);

        //Find relevant buttons
        mIncreaseButton = (Button) findViewById(R.id.increase_button);
        mDecreaseButton = (Button) findViewById(R.id.decrease_button);
        mOrderViaEmailButton = (ImageButton) findViewById(R.id.order_via_email_image_button);
        mOrderViaPhoneButton = (ImageButton) findViewById(R.id.order_via_tel_image_button);
        mLoadImageButton = (Button) findViewById(R.id.load_image_button);

        mImageView = (ImageView) findViewById(R.id.product_picture);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.detail_activity_add_product));
            //hide "Delete" menue option
            invalidateOptionsMenu();
            //hide increase and decrease button
            mIncreaseButton.setVisibility(View.GONE);
            mDecreaseButton.setVisibility(View.GONE);
            //hide order button
            mOrderViaEmailButton.setVisibility(View.GONE);
            mOrderViaPhoneButton.setVisibility(View.GONE);
        } else {
            setTitle(getString(R.string.detail_activity_product_detail));
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mIncreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(mQuantityEditText.getText().toString());
                quantity++;
                mQuantityEditText.setText(Integer.toString(quantity));
                mProductEdited = true;
            }
        });

        mDecreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(mQuantityEditText.getText().toString());
                if (quantity > 0) {
                    quantity--;
                    mQuantityEditText.setText(Integer.toString(quantity));
                    mProductEdited = true;
                }
            }
        });

        mOrderViaEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderViaEmail();
            }
        });

        mOrderViaPhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderViaPhone();
            }
        });

        mLoadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryToOpenImageSelector();
                mProductEdited = true;
            }
        });
    }

    private void orderViaEmail() {
        String emailAddress = mSupplierEmailEditText.getText().toString().trim();
        String productName = mNameEditText.getText().toString().trim();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        if (emailAddress != null) {
            intent.setData(Uri.parse("mailto:" + emailAddress));
        } else {
            intent.setData(Uri.parse("mailto:"));
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, "New Order for " + productName);
        String body = "I'd like to order some " + productName + ",please arrange the delivery as soon as possible.";
        intent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(intent);
    }

    private void orderViaPhone() {
        String phoneNumber = mSupplierTelEditText.getText().toString().trim();
        Intent intent = new Intent(Intent.ACTION_DIAL);
        if (phoneNumber != null) {
            intent.setData(Uri.parse("tel:" + phoneNumber));
        }
        startActivity(intent);
    }

    private void tryToOpenImageSelector() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            return;
        }
        openImageSelector();
    }

    private void openImageSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImageSelector();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                mImageUri = resultData.getData();
                mImageView.setImageURI(mImageUri);
                mImageView.invalidate();
            }
        }
    }

    private void saveProduct() {
        String name = mNameEditText.getText().toString().trim();
        String price = mPriceEditText.getText().toString().trim();
        String quantity = mQuantityEditText.getText().toString().trim();
        String supplier = mSupplierEditText.getText().toString().trim();
        String supplierEmail = mSupplierEmailEditText.getText().toString().trim();
        String supplierTel = mSupplierTelEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getApplicationContext(), getString(R.string.require_name), Toast.LENGTH_LONG).show();
            mNameEditText.setError(getString(R.string.require_name));
            return;
        } else if (TextUtils.isEmpty(price)) {
            Toast.makeText(getApplicationContext(), getString(R.string.require_price), Toast.LENGTH_LONG).show();
            mPriceEditText.setError(getString(R.string.require_price));
            return;
        } else if (TextUtils.isEmpty(quantity)) {
            Toast.makeText(getApplicationContext(), getString(R.string.require_quantity), Toast.LENGTH_LONG).show();
            mQuantityEditText.setError(getString(R.string.require_quantity));
            return;
        } else if (TextUtils.isEmpty(supplier)) {
            Toast.makeText(getApplicationContext(), getString(R.string.require_supplier), Toast.LENGTH_LONG).show();
            mSupplierEditText.setError(getString(R.string.require_supplier));
            return;
        } else if (mImageUri == null && mCurrentProductUri == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.require_image), Toast.LENGTH_LONG).show();
            return;
        }

        double priceDouble = Double.parseDouble(price);
        int quantityInt = Integer.parseInt(quantity);

        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_NAME, name);
        values.put(ProductContract.ProductEntry.COLUMN_PRICE, priceDouble);
        values.put(ProductContract.ProductEntry.COLUMN_QUANTITY, quantityInt);
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER, supplier);
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL, supplierEmail);
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_TEL, supplierTel);
        if(mImageUri!=null) {
            values.put(ProductContract.ProductEntry.COLUMN_IMAGE, mImageUri.toString());
        }

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(DetailActivity.this, R.string.detail_activity_insert_unsuccessfully, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DetailActivity.this, R.string.detail_activity_insert_successfully, Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsUpdated = getContentResolver().update(mCurrentProductUri, values, null, null);
            if (rowsUpdated == 0) {
                Toast.makeText(DetailActivity.this, R.string.detail_activity_update_unsuccessfully, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DetailActivity.this, R.string.detail_activity_update_successfully, Toast.LENGTH_SHORT).show();
            }
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(DetailActivity.this, R.string.detail_activity_delete_unsuccessfully, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DetailActivity.this, R.string.detail_activity_delete_successfully, Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete_item);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                return true;
            case R.id.action_order_more:
                orderMoreDialog();
                return true;
            case R.id.action_delete_item:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mProductEdited) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    }
                };
                showUnsavedChangeDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mProductEdited) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
        showUnsavedChangeDialog(discardButtonClickListener);
    }

    private void showUnsavedChangeDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void orderMoreDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.detail_activity_order_more);
        builder.setPositiveButton(R.string.by_email, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                orderViaEmail();
            }
        });
        builder.setNegativeButton(R.string.by_phone, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                orderViaPhone();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductContract.ProductEntry.COLUMN_ID,
                ProductContract.ProductEntry.COLUMN_NAME,
                ProductContract.ProductEntry.COLUMN_PRICE,
                ProductContract.ProductEntry.COLUMN_QUANTITY,
                ProductContract.ProductEntry.COLUMN_SUPPLIER,
                ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL,
                ProductContract.ProductEntry.COLUMN_SUPPLIER_TEL,
                ProductContract.ProductEntry.COLUMN_IMAGE
        };
        return new CursorLoader(this, mCurrentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            //Find the columns of the product attributes
            int nameColumnIdx = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_NAME);
            int quantityColumnIdx = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_QUANTITY);
            int priceColumnIdx = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRICE);
            int supplierColumnIdx = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_SUPPLIER);
            int supplierEmailColumnIdx = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL);
            int supplierTelColumnIdx = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_SUPPLIER_TEL);
            int imageColumnIdx = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_IMAGE);

            //Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIdx);
            int quantity = cursor.getInt(quantityColumnIdx);
            double price = cursor.getDouble(priceColumnIdx);
            String supplier = cursor.getString(supplierColumnIdx);
            String supplierEmail = cursor.getString(supplierEmailColumnIdx);
            String supplierTel = cursor.getString(supplierTelColumnIdx);
            Uri imageUri = Uri.parse(cursor.getString(imageColumnIdx));

            //Update the views on the screen with the values from database
            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Double.toString(price));
            mSupplierEditText.setText(supplier);
            mSupplierEmailEditText.setText(supplierEmail);
            mSupplierTelEditText.setText(supplierTel);
            mImageView.setImageURI(imageUri);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mSupplierEditText.setText("");
        mSupplierEmailEditText.setText("");
        mSupplierTelEditText.setText("");
        mImageView.setImageResource(R.drawable.no_photo);
    }
}
