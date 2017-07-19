package com.cursoslicad.android.weatherviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by javier on 7/19/17.
 */

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherHolder> {
    private List<Weather> mWeathers;
    private Map<String, Bitmap> bitmaps = new HashMap<>();
    private Context mContext;

    public WeatherAdapter(Context context, List<Weather> weathers) {
        mContext = context;
        mWeathers = weathers;
    }

    public class WeatherHolder extends RecyclerView.ViewHolder {
        public Weather mWeather;
        public ImageView conditionImageView;
        public TextView dayTextView;
        public TextView lowTextView;
        public TextView hiTextView;
        public TextView humidityTextView;

        public WeatherHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item, parent, false));
            conditionImageView = (ImageView) itemView.findViewById(R.id.conditionImageView);
            dayTextView = (TextView) itemView.findViewById(R.id.dayTextView);
            lowTextView = (TextView) itemView.findViewById(R.id.lowTextView);
            hiTextView = (TextView) itemView.findViewById(R.id.hiTextView);
            humidityTextView = (TextView) itemView.findViewById(R.id.humidityTextView);
        }

        public void bind(Weather weather) {
            mWeather = weather;
            dayTextView.setText(mContext.getString(R.string.day_description, mWeather.dayofWeek,
                    mWeather.description));
            lowTextView.setText(mContext.getString(R.string.low_temp, mWeather.minTemp));
            hiTextView.setText(mContext.getString(R.string.high_temp, mWeather.maxTemp));
            humidityTextView.setText(mContext.getString(R.string.humidity, mWeather.humidity));
        }

    }

    @Override
    public WeatherHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new WeatherHolder(layoutInflater, parent);
    }

    @Override
    public void onBindViewHolder(WeatherHolder holder, int position) {
        Weather weather = mWeathers.get(position);
        String iconURL = weather.iconURL;
        if(bitmaps.containsKey(weather.iconURL)){
            holder.conditionImageView.setImageBitmap(bitmaps.get(iconURL));
        }
        else{
            new LoadImageTask(holder.conditionImageView).execute(weather.iconURL);
        }
        holder.bind(weather);
    }

    @Override
    public int getItemCount() {
        return mWeathers.size();
    }


    // AsyncTask para cargar las condiciones climáticas en un hilo separado
    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView; // despliega imagen del clima

        // se tiene el ImageView donde se va a guardar la imagen descargada
        public LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        // Se carga la imagen, params[0] tiene la URL donde se encuentra la imagen
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            HttpURLConnection connection = null;

            try {
                URL url = new URL(params[0]); // create URL for image

                // Se abre una HttpURLConnection, se guarda en un input InputStream
                // y se descarga la imagen

                connection = (HttpURLConnection) url.openConnection();

                try (InputStream inputStream = connection.getInputStream()) {
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    bitmaps.put(params[0], bitmap); // cache for later use
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                connection.disconnect(); // close the HttpURLConnection
            }

            return bitmap;
        }

        // se coloca la imagen en el ImageView
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }


}

