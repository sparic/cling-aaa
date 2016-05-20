package org.fourthline.cling.demo.android.browser;

import java.io.IOException;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * Created by Ray.Fu on 2016/5/18.
 */
public class PictureActivity extends Activity{


        private final String IMAGE_TYPE = "image/*";

        private final int IMAGE_CODE = 0;   //这里的IMAGE_CODE是自己任意定义的

        private Button addPic=null,showPicPath=null;

        private ImageView imgShow=null;

        private TextView imgPath=null;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_picture);
            init();
        }

        private void init() {
            // TODO Auto-generated method stub

            addPic=(Button) findViewById(R.id.btnClose);
            showPicPath=(Button) findViewById(R.id.btnSend);
            imgPath=(TextView) findViewById(R.id.img_path);
            imgShow=(ImageView) findViewById(R.id.imgShow);

            addPic.setOnClickListener(listener);

            showPicPath.setOnClickListener(listener);

        }
        private OnClickListener listener=new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub


                Button btn=(Button) v;

                switch(btn.getId()){

                    case R.id.btnClose:
                        setImage();
                        break;

                    case R.id.btnSend:

                        break;
                }

            }



            private void setImage() {
                // TODO Auto-generated method stub
                //使用intent调用系统提供的相册功能，使用startActivityForResult是为了获取用户选择的图片



                Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);

                getAlbum.setType(IMAGE_TYPE);

                startActivityForResult(getAlbum, IMAGE_CODE);


            }};
//        @Override
//        public boolean onCreateOptionsMenu(Menu menu) {
//            getMenuInflater().inflate(R.menu.activity_picture, menu);
//            return true;
//        }

        protected void onActivityResult(int requestCode, int resultCode, Intent data){

            if (resultCode != RESULT_OK) {        //此处的 RESULT_OK 是系统自定义得一个常量

                Log.e("TAG->onresult","ActivityResult resultCode error");

                return;

            }



            Bitmap bm = null;



            //外界的程序访问ContentProvider所提供数据 可以通过ContentResolver接口

            ContentResolver resolver = getContentResolver();



            //此处的用于判断接收的Activity是不是你想要的那个

            if (requestCode == IMAGE_CODE) {

                try {

                    Uri originalUri = data.getData();        //获得图片的uri

//                    String wholeID = DocumentsContract.getDocumentId(originalUri);

                    bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);
                    //显得到bitmap图片
                    imgShow.setImageBitmap(bm);


//    这里开始的第二部分，获取图片的路径：



                    String[] proj = {MediaStore.Images.Media.DATA};



                    //好像是android多媒体数据库的封装接口，具体的看Android文档

                    Cursor cursor =  getContentResolver().query(originalUri, proj, null, null, null);

                    //按我个人理解 这个是获得用户选择的图片的索引值

                    //将光标移至开头 ，这个很重要，不小心很容易引起越界
                    cursor.moveToFirst();

                    int column_index = cursor.getColumnIndex(proj[0]);



                    //最后根据索引值获取图片路径
                    String path = cursor.getString(column_index);
//                    String path = cursor.getString(1);
                    cursor.close();

                    imgPath.setText(path);
                }catch (IOException e) {

                    Log.e("TAG-->Error",e.toString());

                }

            }

        }
}
