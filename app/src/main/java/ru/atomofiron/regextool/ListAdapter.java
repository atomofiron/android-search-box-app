package ru.atomofiron.regextool;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

public class ListAdapter extends BaseAdapter { // Почему BaseAdapter, потому что он прост и выполняет свою задачу

    Context context;
    ArrayList<File> filesList;
    ArrayList<String> linesList;
    Map<Integer,Boolean> boolList;
    SharedPreferences sp;

    int length = 1;
    boolean absolutePaths=false;
    boolean ex = false;
    boolean checkable = false;


    ListAdapter(Context context) {
        this.context = context;
        filesList = new ArrayList<>();
        linesList = new ArrayList<>();
        boolList = new Hashtable<>();
        sp = context.getSharedPreferences(I.PREFS,Context.MODE_PRIVATE);
        String path = sp.getString(I.STORAGE_PATH,"/");
        length += path.length();
    }

    void addFile(File file) {
        if (filesList.contains(file)) return;
        filesList.add(file);
        accept();
    }

    void setList(File[] list) {
        if (checkable) boolList.clear();
        filesList.clear();
        Collections.addAll(filesList, list);
        Collections.sort(filesList);
        accept();
    }
    void setList(ArrayList<String> list) {
        if (checkable) boolList.clear();
        filesList.clear();
        for (String path : list) filesList.add(new File(path));
        accept();
    }
    void setLinesList(ArrayList<String> list) {
        if (list.size()>0) {
            linesList = list;
            ex=true;
        }
    }
    void remove(int position) {
        if (checkable && boolList.containsKey(position)) boolList.remove(position);
        filesList.remove(position);
        notifyDataSetChanged();
    }
    void accept() {
        int end = filesList.size();
        for (int i=0;i<end;i++) if (!filesList.get(i).isDirectory()) {
            filesList.add(filesList.remove(i));
            i--;
            end--;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return filesList.size();
    }

    @Override
    public File getItem(int position) {
        return filesList.get(position);
    }

    public ArrayList<String> getPathArray() {
        ArrayList<String> list = new ArrayList<>();

        if (getCount()>0)
            for (int i=0;i<getCount();i++) {
                Object b = boolList.get(i);
                if (b == null || (boolean) b) list.add(filesList.get(i).getAbsolutePath());
            }
            //for (File file : filesList) list.add(file.getAbsolutePath());
        else list.add(sp.getString(I.STORAGE_PATH,"/"));
        return list;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    static class ViewHolder {
        TextView text;
        ImageView icon;
        CheckBox check;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View myView = convertView;
        ViewHolder holder;

        if (myView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            myView = inflater.inflate(R.layout.item, null, true);
            holder = new ViewHolder();
            holder.text = (TextView)myView.findViewById(R.id.text);
            holder.icon = (ImageView)myView.findViewById(R.id.icon);
            holder.check = (CheckBox)myView.findViewById(R.id.checkbox);
            if (checkable) holder.check.setVisibility(View.VISIBLE);

            myView.setTag(holder);
        } else holder = (ViewHolder) myView.getTag();

        String name;
        if (absolutePaths) {
            I.Log("length="+length);
            I.Log("path="+filesList.get(position).getAbsolutePath().length());
            name = filesList.get(position).getAbsolutePath().substring(length);
        } else name = filesList.get(position).getName();
        if (ex) name = linesList.get(position)+name;
        holder.text.setText(name);
        holder.icon.setImageResource(filesList.get(position).isDirectory()?R.drawable.ic_folder:R.drawable.ic_file);
        I.Log("check "+(!boolList.containsKey(position) || boolList.get(position)));
        holder.check.setChecked(!boolList.containsKey(position) || boolList.get(position));
        return myView;
    }

    public void onItemClick(int position) {
        I.Log("onItemClick() "+(boolList.containsKey(position) && !boolList.get(position)));
        boolList.put(position,boolList.containsKey(position) && !boolList.get(position));
        notifyDataSetChanged();
    }
}
