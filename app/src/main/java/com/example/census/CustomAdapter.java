package com.example.census;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomAdapter extends ArrayAdapter<CustomModelList> {

    Context context;
    java.util.List<CustomModelList> arrayList;
    ArrayList<CustomModelList> arrayPam;


    public CustomAdapter(Context context, int resource, List<CustomModelList> arrayList) {
        super(context, resource, arrayList);
        this.context = context;
        this.arrayList = arrayList;
        this.arrayPam = new ArrayList<CustomModelList>();
        this.arrayPam.addAll(arrayList);
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public CustomModelList getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.custom_list_people, parent, false);

        CircularImageView imaj = (CircularImageView) convertView.findViewById(R.id.profilePi);
            TextView name = (TextView) convertView.findViewById(R.id.textName);
            TextView cin = (TextView) convertView.findViewById(R.id.textCin);

            loadImg(arrayList.get(position).getImage(),imaj);
            name.setText(arrayList.get(position).getName());
            cin.setText(arrayList.get(position).getCin());

        return convertView;
    }

    public void filter(String chartext)
    {
        chartext = chartext.toLowerCase(Locale.getDefault());
        arrayList.clear();
        if(chartext.length()==0)
        {
            arrayList.addAll(arrayPam);
        }
        else
        {
            for (CustomModelList customModelList : arrayPam)
            {
                if (customModelList.getName().toLowerCase(Locale.getDefault()).contains(chartext))
                {
                    arrayList.add(customModelList);
                }
                else if (customModelList.getCin().toLowerCase(Locale.getDefault()).contains(chartext))
                {
                    arrayList.add(customModelList);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void loadImageFromStorage(String path, ImageView view)
    {
        try
        {
            File file = new File(path, "profile.jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
            view.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void loadImg(String image, ImageView view)
    {
        byte[] decodedString = Base64.decode(image,Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString,0,decodedString.length);
        view.setImageBitmap(Bitmap.createScaledBitmap(decodedByte,130,130,true));
    }



}
