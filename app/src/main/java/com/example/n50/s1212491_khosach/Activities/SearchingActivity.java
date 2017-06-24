package com.example.n50.s1212491_khosach.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.n50.s1212491_khosach.Adapters.BookListAdapter;
import com.example.n50.s1212491_khosach.Common.Book9;
import com.example.n50.s1212491_khosach.Common.DBHelper;
import com.example.n50.s1212491_khosach.Common.MyApplication;
import com.example.n50.s1212491_khosach.Progress.LongOperation;
import com.example.n50.s1212491_khosach.R;

import java.util.List;


public class SearchingActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    LongOperation mLongOperation;
    private EditText mSearchKey_et;
    private ImageButton mSearchButton_ib;
    private ListView mResultList_lv;

    private List<Book9> mBook9List;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching);

//        mLongOperation = new LongOperation(this);//TO UNCOMMENT
        mSearchKey_et = (EditText) findViewById(R.id.search_title_tv);
        mSearchButton_ib = (ImageButton) findViewById(R.id.search_search_btn);
        mResultList_lv = (ListView) findViewById(R.id.search_result_list_lv);
        mResultList_lv.setEmptyView(findViewById(R.id.empty));
        mResultList_lv.setOnItemClickListener(this);

        mSearchButton_ib.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_search_btn:
                mBook9List = null;
                String searchKey = mSearchKey_et.getText().toString();
                if (searchKey.equals("")) {
                    Toast.makeText(this, "Vui lòng nhập từ khoá", Toast.LENGTH_SHORT).show();
                    return;
                }
//                mLongOperation.searchBookTask(searchKey, mResultList_lv, this);//TO UNCOMMENT

                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                break;

            default:
                super.onClick(v);
                break;
        }
    }


    public void setAdapterForList(List<Book9> list) {
        mBook9List = list;
//        BookListAdapter adapter = new BookListAdapter(this, mBook9List);//TO UNCOMMENT
//        mResultList_lv.setAdapter(adapter);//TO UNCOMMENT
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent detailIntent = new Intent(this, DetailActivity.class);
        detailIntent.putExtra("selectedBook", mBook9List.get(position));
        this.startActivity(detailIntent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        DBHelper db = ((MyApplication) getApplication()).getmLocalDatabase();
                        if (db.checkIfExistDownloadedBook(mBook9List.get(position).getId()) == true) {
                            Toast.makeText(SearchingActivity.this, mBook9List.get(position).getTitle().toUpperCase() + " đã tồn tại", Toast.LENGTH_SHORT).show();
                        } else {
                            db.insertDownloadedBook(mBook9List.get(position).getId(), mBook9List.get(position).getTitle(), mBook9List.get(position).getAuthor(), mBook9List.get(position).getCoverUrl());

                            //tải và thêm các chapter của book xuống local database
                            mLongOperation.getAllChaptersTask(mBook9List.get(position).getId());
                            ((MyApplication) getApplication()).setmLocalDatabase(db);
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        //////////
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchingActivity.this);
        builder.setMessage("Bạn có muốn thêm truyện " + mBook9List.get(position).getTitle().toUpperCase() + " vào TRUYỆN CỦA TÔI không?")
                .setPositiveButton("Có", dialogClickListener)
                .setNegativeButton("Không", dialogClickListener).show();
        return true;
    }
}
