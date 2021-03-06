package test.andy.tubeswisata;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Response;
import test.andy.tubeswisata.model.wisata;
import test.andy.tubeswisata.network.Api;
import test.andy.tubeswisata.util.Util;

public class listWisataActivityAdmin extends AppCompatActivity {
    private listWisataActivityAdmin obj;
    private AdapterWisata adapter;
    private android.support.v7.widget.Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wisata);

        ListView lsvw_data=findViewById(R.id.id_list);
        Button btn_tambah=findViewById(R.id.btn_tambah);
        obj = this;
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setTitle("[Admin] Wisata");

        btn_tambah.setVisibility(View.VISIBLE);
        adapter=new AdapterWisata(this);
        lsvw_data.setAdapter(adapter);


        lsvw_data.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                showDialogListOpsi(j -> {
                    if (j == 0) {
                        adapter.getItem(position);

                        startActivityForResult(new Intent(listWisataActivityAdmin.this, TambahWisata.class)
                                .putExtra("id",adapter.getItem(position).getId())
                                .putExtra("nama",adapter.getItem(position).getNama())
                                .putExtra("lnglat",adapter.getItem(position).getLnglat())
                                .putExtra("deskripsi",adapter.getItem(position).getDeskripsi())
                                .putExtra("foto",adapter.getItem(position).getFoto())
                                .putExtra("kategori",adapter.getItem(position).getKategori()),
                                1);

                    }else if (j == 1) {
                        adapter.getItem(position);
                        AlertDialog.Builder builder = new AlertDialog.Builder(listWisataActivityAdmin.this);
                        builder.setMessage("item akan dihapus Lanjutkan?");
                        builder.setPositiveButton("YA", (dialogInterface, i) -> {
                            delete(adapter.getItem(position).getId());
                        });
                        builder.setNegativeButton("TIDAK", (dialogInterface, i) -> {
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }
                }, listWisataActivityAdmin.this);

            }

        });


        btn_tambah.setOnClickListener(view -> {
            startActivityForResult(new Intent(this, TambahWisata.class).putExtra("add image",true),1);
        });

        getRecord();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            getRecord();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                getRecord();
            }
            if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    public boolean getRecord() {
        boolean[] a = {false};
        new AsyncTask<Void, Void, Boolean>() {
            Date dStart = null;
            Date dEnd = null;
            ProgressDialog dialog =new ProgressDialog(listWisataActivityAdmin.this);

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog.setMessage("Loading get data...");

                obj.runOnUiThread(new Runnable() {
                    public void run() {
                        dialog.show();
                    }
                });
                dStart = new Date();
                dEnd = new Date();

            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean b=false;

                try (Response response = new Api(listWisataActivityAdmin.this).
                        get(getString(R.string.api_wisata))) {
                    if (response == null || !response.isSuccessful())
                        throw new IOException("Unexpected code = " + response);

                    String responseBodyString = response.body().string();
                    JSONObject responseBodyObject = new JSONObject(responseBodyString);

                    List<wisata.Data> data =new ArrayList<>();

                    if (responseBodyObject.getBoolean("status")) {
                        Gson gson = new Gson();
                        wisata fromJson = gson.fromJson(responseBodyString, wisata.class);

                        for (int x = 0; x < fromJson.getData().size(); x++) {

                            data.add(fromJson.getData().get(x));

                        }


                        obj.runOnUiThread(new Runnable() {

                            public void run() {
                                adapter.setList(data);

                            }
                        });
                    }else {
                        obj.runOnUiThread(new Runnable() {
                            public void run() {
                                adapter.setList(data);
                                Toast.makeText(obj, "Tidak Ada data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (IOException e) {
                    obj.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(obj, "Terjadi Respon error server", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return  b;
            }

            @Override
            protected void onPostExecute(Boolean aVoid) {
                super.onPostExecute(aVoid);
                obj.runOnUiThread(new Runnable() {
                    public void run() {
                        if (dialog!=null&dialog.isShowing()){
                            dialog.dismiss();
                        }
                    }
                });


            }
        }.execute();


        return  a[0];

    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        showdialogKeluar();
    }

    public void showdialogKeluar(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Apakah anda ingin Logout?");
        builder.setPositiveButton("YA", (dialogInterface, i) -> {
          finish();
        });
        builder.setNegativeButton("TIDAK", (dialogInterface, i) -> {
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public boolean delete(String id) {
        boolean[] a = {false};

        new AsyncTask<Void, Void, Boolean>() {
            ProgressDialog dialog =new ProgressDialog(listWisataActivityAdmin.this);

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog.setMessage("Loading update data...");

                obj.runOnUiThread(new Runnable() {
                    public void run() {
                        dialog.show();
                    }
                });
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean b=false;
                FormBody.Builder formBody = new FormBody.Builder()
                        .add("id", id)
                        .add("action", "DELETE");
                try (Response response = new Api(listWisataActivityAdmin.this).
                        post(getString(R.string.api_wisata),formBody)) {
                    if (response == null || !response.isSuccessful())
                        throw new IOException("Unexpected code = " + response);

                    String responseBodyString = response.body().string();
                    JSONObject responseBodyObject = new JSONObject(responseBodyString);
                    if (responseBodyObject.getBoolean("status")) {

                        obj.runOnUiThread(new Runnable() {
                            public void run() {
                                if (dialog!=null&dialog.isShowing()){
                                    dialog.dismiss();
                                }


                                Toast.makeText(obj, "Berhasil di hapus", Toast.LENGTH_SHORT).show();
                                getRecord();

                            }
                        });
                    }else {
                        obj.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(obj, "Return false ,kesalahan data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (IOException e) {
                    obj.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(obj, "Terjadi Respon error server", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return  b;
            }

            @Override
            protected void onPostExecute(Boolean aVoid) {
                super.onPostExecute(aVoid);
                obj.runOnUiThread(new Runnable() {
                    public void run() {
                        if (dialog!=null&dialog.isShowing()){
                            dialog.dismiss();
                        }
                    }
                });


            }
        }.execute();


        return  a[0];

    }

    public interface ListenerDialog {
        void onClick(int i);
    }

    public void showDialogListOpsi(ListenerDialog listener, Context context) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context, R.style.Theme_Dialog_Margin_4);
        List<String> where = new ArrayList<String>();

        where.add("Edit");
        where.add("Hapus");

        String[] strings = new String[where.size()];
        where.toArray(strings);

        builder.setItems(strings, (dialog, which) -> {
            switch (which) {
                case 0:
                    listener.onClick(0);
                    break;
                case 1:
                    listener.onClick(1);
                    break;

                default:
                    listener.onClick(which);
            }
        });

        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(alertDialog.getWindow().getAttributes());
        lp.width = Util.toDIP(context, 360);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        alertDialog.getWindow().setAttributes(lp);
    }


}
