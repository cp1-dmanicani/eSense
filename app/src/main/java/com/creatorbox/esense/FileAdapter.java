/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

/**
 *
 */
public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder>{

    Context context;
    File[] filesInternal;

    public FileAdapter(Context context, File[] filesInternal){
        this.context = context;
        this.filesInternal = filesInternal;
    }

    //List all folders in internal storage
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View viewInternal = LayoutInflater.from(context).inflate(R.layout.recycler_item,parent,false);
        return new ViewHolder(viewInternal);
    }

    @Override
    public void onBindViewHolder(FileAdapter.ViewHolder holder, int position) {

        File selectedFile = filesInternal[position];
        holder.textView.setText(selectedFile.getName());

        if(selectedFile.isDirectory()){
            holder.imageView.setImageResource(R.drawable.ic_baseline_folder_24);
        }else{
            holder.imageView.setImageResource(R.drawable.ic_baseline_insert_drive_file_24);
        }

        holder.itemView.setOnClickListener(v -> {
            if(selectedFile.isDirectory()){
                Intent intent = new Intent(context, FileListActivityInternalStorage.class);
                String path = selectedFile.getAbsolutePath();
                intent.putExtra("path",path);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
            else {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    String type = "*/*";
                    intent.setDataAndType(Uri.parse(selectedFile.getAbsolutePath()), type);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Exception e){
                    Toast.makeText(context.getApplicationContext(),"Cannot open the file",Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.itemView.setOnLongClickListener(v -> {

            holder.itemView.setBackgroundColor(Color.GRAY);
            PopupMenu popupMenu = new PopupMenu(context,v);
            popupMenu.getMenu().add("DELETE");
            popupMenu.getMenu().add("MOVE");
            popupMenu.getMenu().add("RENAME");

            popupMenu.setOnMenuItemClickListener(item -> {
                if(item.getTitle().equals("DELETE")){
                    boolean deleted = selectedFile.delete();
                    if(deleted){
                        Toast.makeText(context.getApplicationContext(),"DELETED ",Toast.LENGTH_SHORT).show();
                        v.setVisibility(View.GONE);
                    }
                }
                if(item.getTitle().equals("MOVE")){
                    Toast.makeText(context.getApplicationContext(),"MOVED ",Toast.LENGTH_SHORT).show();
                }
                if(item.getTitle().equals("RENAME")){
                    Toast.makeText(context.getApplicationContext(),"RENAME ",Toast.LENGTH_SHORT).show();
                }
                return true;
            });
            popupMenu.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return filesInternal.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView textView;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.file_name_text_view);
            imageView = itemView.findViewById(R.id.icon_view);
        }
    }
}