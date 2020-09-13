package com.example.filemanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    LinearLayout linearLayout;
    ImageButton imageButton, newFolder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        linearLayout = findViewById(R.id.ll);
        imageButton = findViewById(R.id.imageButton);
        newFolder = findViewById(R.id.newFolder);

    }


        class TextAdapter extends BaseAdapter{

        private boolean[] selection;

        private List<String> data = new ArrayList<>();

        public void setData(List<String> data) {
                if(data != null){
                    this.data.clear();
                    if(data.size() > 0){
                        this.data.addAll(data);
                    }
                    notifyDataSetChanged();
                }
            }

            void setSelection(boolean[] selection){
                if(selection != null){
                    this.selection = new boolean[selection.length];
                    for (int i=0; i<selection.length; i++){
                        this.selection[i] = selection[i];
                    }
                    notifyDataSetChanged();
                }
            }


            @Override
            public int getCount() {
                return data.size();
            }

            @Override
            public String getItem(int position) {
                return data.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }


            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
                    convertView.setTag(new ViewHolder((TextView) convertView.findViewById(R.id.textItem)));
                }
                ViewHolder holder = (ViewHolder) convertView.getTag();
                final String item = getItem(position);
                holder.info.setText(item.substring(item.lastIndexOf("/")+1));
                if(selection!=null){
                    if(selection[position]){
                        holder.info.setBackgroundColor(Color.WHITE);
                        holder.info.setTextColor(Color.BLACK);
                    }
                    else {
                        holder.info.setBackgroundColor(getResources().getColor(R.color.darkgrey));
                        holder.info.setTextColor(getResources().getColor(R.color.white));
                    }
                }
                return convertView;
            }

            class ViewHolder{
                TextView info;

                ViewHolder(TextView info){
                    this.info = info;
                }
            }
        }

    private static final int REQUEST_PERMISSION = 6969;

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int PERMISSIONS_COUNT = 2;

    @SuppressLint("NewAPI")
    private Boolean isPermissionDenied(){
        int i=0;
        while (i < PERMISSIONS_COUNT){
            if(checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED){
                return true;
            }
            i++;
        }
        return false;
    }
    private boolean isFileManagerCreated = false;

    private boolean[] selection;

    private File[] files;
    List<String> fileList;

    private int filesCount;
    TextView pathOutput;
    File dir;
    String rootPath;
    int selectedItemIndex;
    Button delete;
    Button rename;
    Button copy;
    Button cut;
    Button paste;
    private String copyPath;
    TextAdapter textAdapter;


    @Override
    protected void onResume(){
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isPermissionDenied()){
                requestPermissions(PERMISSIONS, REQUEST_PERMISSION);
                return;
        }
        if(!isFileManagerCreated){
            rootPath = String.valueOf(getExternalFilesDir(Environment.DIRECTORY_DCIM));

            dir = new File(rootPath);
            files = dir.listFiles();
            pathOutput = findViewById(R.id.pathOutput);
            pathOutput.setText(rootPath.substring(rootPath.lastIndexOf("/")+1));
            filesCount = files.length;
            final ListView listView = findViewById(R.id.listView);

            textAdapter = new TextAdapter();

            listView.setAdapter(textAdapter);

            fileList = new ArrayList<>();

            for(int i=0; i<filesCount; i++){
                fileList.add(String.valueOf(files[i].getAbsolutePath()));

            }

            textAdapter.setData(fileList);
            selection = new boolean[files.length];

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    rootPath = files[position].getAbsolutePath();
                    dir = new File(rootPath);
                    files = dir.listFiles();
                    pathOutput.setText(rootPath.substring(rootPath.lastIndexOf("/")+1));
                    filesCount = files.length;
                    fileList.clear();
                    for(int i=0; i<filesCount; i++){
                        fileList.add(String.valueOf(files[i].getAbsolutePath()));
                    }

                    textAdapter.setData(fileList);
//                    selection = new boolean[files.length];
//                    textAdapter.setSelection(selection);


                }
            });

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    selection[position] = !selection[position];
                    textAdapter.setSelection(selection);
                    int selectedCount = 0;
                    for (int i=0; i<files.length; i++){
                        if(selection[i]){
                            selectedCount++;
                        }
                        if(selectedCount > 0){
                            if(selectedCount ==  1){
                                selectedItemIndex = position;
                                rename.setVisibility(View.VISIBLE);
                            }
                            else {
                                rename.setVisibility(View.GONE);
                            }
                            linearLayout.setVisibility(View.VISIBLE);
                        }
                        else {
                            linearLayout.setVisibility(View.GONE);
                        }
                    }

                    return true;
                }
            });

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rootPath =  rootPath.substring(0, rootPath.lastIndexOf("/"));
                    dir = new File(rootPath);
                    files = dir.listFiles();
                    pathOutput.setText(rootPath.substring(rootPath.lastIndexOf("/")+1));
                    filesCount = files.length;
                    fileList.clear();
                    for(int i=0; i<filesCount; i++){
                        fileList.add(String.valueOf(files[i].getAbsolutePath()));
                    }

                    textAdapter.setData(fileList);
                    selection = new boolean[files.length];
                    textAdapter.setSelection(selection);


                }
            });


            copy = findViewById(R.id.copy);
            cut = findViewById(R.id.cut);
            rename = findViewById(R.id.rename);
            delete = findViewById(R.id.delete);
            paste = findViewById(R.id.paste);

            cut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    copyPath = files[selectedItemIndex].getAbsolutePath();
                    paste.setVisibility(View.VISIBLE);
                    copy.setVisibility(View.GONE);
                    cut.setVisibility(View.GONE);
                    rename.setVisibility(View.GONE);
                    delete.setVisibility(View.GONE);
                    selection = new boolean[files.length];
                    textAdapter.setSelection(selection);
                    deleteFileOrFolder(files[selectedItemIndex]);
                }
            });


            copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    copyPath = files[selectedItemIndex].getAbsolutePath();
                    Toast.makeText(MainActivity.this, copyPath, Toast.LENGTH_LONG).show();

                    paste.setVisibility(View.VISIBLE);
                    copy.setVisibility(View.GONE);
                    cut.setVisibility(View.GONE);
                    rename.setVisibility(View.GONE);
                    delete.setVisibility(View.GONE);
                    selection = new boolean[files.length];
                    textAdapter.setSelection(selection);
                }
            });

            paste.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    paste.setVisibility(View.GONE);
                    cut.setVisibility(View.VISIBLE);
                    copy.setVisibility(View.VISIBLE);
                    rename.setVisibility(View.VISIBLE);
                    delete.setVisibility(View.VISIBLE);
                    String destinationPath = rootPath + copyPath.substring(copyPath.lastIndexOf("/"));
                    final File newFolder = new File(destinationPath);
                    if(!newFolder.exists()) {
                        newFolder.mkdir();
                    }
                        copyFunc(new File(copyPath), new File(destinationPath));
                        files = new File(rootPath).listFiles();
                        selection = new boolean[files.length];
                        textAdapter.setSelection(selection);
                        filesCount = files.length;
                        fileList.clear();
                        for (int i = 0; i < filesCount; i++) {
                            fileList.add(String.valueOf(files[i].getAbsolutePath()));
                        }
                        textAdapter.setData(fileList);

                }
            });

            rename.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder renameDialog = new AlertDialog.Builder(MainActivity.this);
                    renameDialog.setTitle("Rename To:");
                    final EditText input = new EditText(MainActivity.this);
                    final String renamePath = files[selectedItemIndex].getAbsolutePath();
                    input.setText(renamePath.substring(renamePath.lastIndexOf("/")+1));
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    renameDialog.setView(input);
                    renameDialog.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String s = new File(renamePath).getParent() + "/" + input.getText();
                            File newFile = new File(s);
                            new File(renamePath).renameTo(newFile);
                            files = dir.listFiles();
                            filesCount = files.length;
                            fileList.clear();
                            for(int i=0; i<filesCount; i++){
                                fileList.add(String.valueOf(files[i].getAbsolutePath()));
                            }
                            selection[selectedItemIndex] = false;
                            textAdapter.setSelection(selection);
                            linearLayout.setVisibility(View.GONE);

                            textAdapter.setData(fileList);
                        }
                    });
                    renameDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    renameDialog.show();
                }
            });

            newFolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder newFolderDialog = new AlertDialog.Builder(MainActivity.this);
                    newFolderDialog.setTitle("New Folder");
                    final EditText input = new EditText(MainActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    newFolderDialog.setView(input);
                    newFolderDialog.setPositiveButton("Create New Folder", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final File newFolder = new File(rootPath + "/" + input.getText());
                            if(!newFolder.exists()){
                                newFolder.mkdir();

                                files = dir.listFiles();
                                filesCount = files.length;
                                fileList.clear();
                                for(int i=0; i<filesCount; i++){
                                    fileList.add(String.valueOf(files[i].getAbsolutePath()));
                                }

                                textAdapter.setData(fileList);

                            }
                        }
                    });
                    newFolderDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    newFolderDialog.show();
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(MainActivity.this);
                    deleteDialog.setTitle("Delete");
                    deleteDialog.setMessage("Do You Really Want To Delete The Selected Files");
                    deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for(int i=0; i<files.length; i++){
                                if(selection[i]){
                                    deleteFileOrFolder(files[i]);
                                    selection[i] = false;
                                    textAdapter.setSelection(selection);
                                    linearLayout.setVisibility(View.GONE);
                                }
                            }
                            files = dir.listFiles();
                            filesCount = files.length;
                            fileList.clear();
                            for(int i=0; i<filesCount; i++){
                                fileList.add(String.valueOf(files[i].getAbsolutePath()));
                            }

                            textAdapter.setData(fileList);

                        }
                    });
                    deleteDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    deleteDialog.show();
                }
            });

            isFileManagerCreated = true;
        }
        else{
            files = dir.listFiles();
            filesCount = files.length;
            fileList.clear();
            for(int i=0; i<filesCount; i++){
                fileList.add(String.valueOf(files[i].getAbsolutePath()));
            }

            textAdapter.setData(fileList);
        }

    }

    private void copyFunc(File source, File destination){
        try {
            InputStream in = new FileInputStream(source);
            OutputStream out = new FileOutputStream(destination);
            byte[] buf = new byte[1024];
            int length;
            while((length = in.read(buf))>0){
                out.write(buf, 0, length);
            }
            out.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteFileOrFolder(File fileOrFolder){
        if(fileOrFolder.isDirectory()){
            if(fileOrFolder.list().length == 0){
                fileOrFolder.delete();
            }
            else{
                String files[] = fileOrFolder.list();
                for(String temp:files){
                    File fileToDelete = new File(fileOrFolder, temp);
                    deleteFileOrFolder(fileToDelete);
                }
                if(fileOrFolder.list().length == 0){
                    fileOrFolder.delete();
                }
            }
        }
        else {
            fileOrFolder.delete();
        }

    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSION && grantResults.length>0){
            if(isPermissionDenied()){
                ((ActivityManager) Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
                recreate();
            }
            else {
                onResume();
            }
        }
    }



}