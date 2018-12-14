package com.zbform.penform.downmusic;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.zbform.penform.ZBformApplication;
import com.zbform.penform.json.MusicDetailInfo;
import com.zbform.penform.json.MusicFileDownInfo;
import com.zbform.penform.net.BMA;
import com.zbform.penform.net.HttpUtil;
import com.zbform.penform.util.IConstants;
import com.zbform.penform.util.PreferencesUtility;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * Created by isaac on 2018/8/2.
 */
public class Down {


    public static void downMusic(final Context context, final String id, final String name, final String artist) {


        new AsyncTask<String, String, MusicFileDownInfo>() {
            @Override
            protected MusicFileDownInfo doInBackground(final String... name) {
                try {
                    JsonArray jsonArray = HttpUtil.getResposeJsonObject(BMA.Song.songInfo(id).trim()).get("songurl")
                            .getAsJsonObject().get("url").getAsJsonArray();
                    int len = jsonArray.size();

                    int downloadBit = PreferencesUtility.getInstance(context).getDownMusicBit();
                    MusicFileDownInfo musicFileDownInfo;
                    for (int i = len - 1; i > -1; i--) {
                        int bit = Integer.parseInt(jsonArray.get(i).getAsJsonObject().get("file_bitrate").toString());
                        if (bit == downloadBit) {
                            musicFileDownInfo = ZBformApplication.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);
                            return musicFileDownInfo;
                        } else if (bit < downloadBit && bit >= 64) {
                            musicFileDownInfo = ZBformApplication.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);
                            return musicFileDownInfo;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(MusicFileDownInfo musicFileDownInfo) {
                if (musicFileDownInfo != null && musicFileDownInfo.getShow_link() != null) {
                    Intent i = new Intent(DownService.ADD_DOWNTASK);
                    i.setAction(DownService.ADD_DOWNTASK);
                    i.putExtra("id", id);
                    i.putExtra("name", name);
                    i.putExtra("artist", artist);
                    i.putExtra("url", musicFileDownInfo.getShow_link());
                    i.setPackage(IConstants.PACKAGE);
                    context.startService(i);
                }else {
                    Toast.makeText(context,"该歌曲没有下载连接",Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    public static MusicFileDownInfo getUrl(final Context context, final String id) {
        MusicFileDownInfo musicFileDownInfo = null;
        try {
            JsonArray jsonArray = HttpUtil.getResposeJsonObject(BMA.Song.songInfo(id).trim(), context, false).get("songurl")
                    .getAsJsonObject().get("url").getAsJsonArray();
            int len = jsonArray.size();
            int downloadBit = 192;

            for (int i = len - 1; i > -1; i--) {
                int bit = Integer.parseInt(jsonArray.get(i).getAsJsonObject().get("file_bitrate").toString());
                if (bit == downloadBit) {
                    musicFileDownInfo = ZBformApplication.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);

                } else if (bit < downloadBit && bit >= 64) {
                    musicFileDownInfo = ZBformApplication.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return musicFileDownInfo;
    }

    public static MusicDetailInfo getInfo(final String id) {
        MusicDetailInfo info = null;
        try {
            JsonObject jsonObject = HttpUtil.getResposeJsonObject(BMA.Song.songBaseInfo(id).trim()).get("result")
                    .getAsJsonObject().get("items").getAsJsonArray().get(0).getAsJsonObject();
            info = ZBformApplication.gsonInstance().fromJson(jsonObject, MusicDetailInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return info;
    }


    static class getUrl extends Thread {
        boolean isRun = true;
        String id;
        MusicFileDownInfo musicFileDownInfo;

        public getUrl(String id) {
            this.id = id;
        }

        @Override
        public void run() {
            JsonArray jsonArray = HttpUtil.getResposeJsonObject(BMA.Song.songInfo(id).trim()).get("songurl")
                    .getAsJsonObject().get("url").getAsJsonArray();
            int len = jsonArray.size();

            int downloadBit = 128;

            for (int i = len - 1; i > -1; i--) {
                int bit = Integer.parseInt(jsonArray.get(i).getAsJsonObject().get("file_bitrate").toString());
                if (bit == downloadBit) {
                    musicFileDownInfo = ZBformApplication.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);

                } else if (bit < downloadBit && bit >= 64) {
                    musicFileDownInfo = ZBformApplication.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);
                }
            }
        }
    }

}
