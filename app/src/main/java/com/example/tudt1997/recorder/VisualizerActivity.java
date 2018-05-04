package com.example.tudt1997.recorder;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.tudt1997.recorder.permissions.PermissionsActivity;
import com.example.tudt1997.recorder.permissions.PermissionsChecker;
import com.example.tudt1997.recorder.renderer.RendererFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class VisualizerActivity extends AppCompatActivity {

    //    private static final int CAPTURE_SIZE = 256;
    private static final int REQUEST_CODE = 0;
    static final String[] PERMISSIONS = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET};

    AudioRecord recorder;

    private int sampleRate = 16000; // 44100 for music
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize;
    private volatile boolean status;

    private int port = 50005;

    private WaveformView waveformView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizer);
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
//        setSupportActionBar(toolbar);

        waveformView = findViewById(R.id.waveform_view);
        RendererFactory rendererFactory = new RendererFactory();
        waveformView.setRenderer(rendererFactory.createSimpleWaveformRenderer(Color.GREEN, Color.DKGRAY));

        startVisualizing();
    }

    public void startVisualizing() {
        Thread streamThread = new Thread(new Runnable() {
            @Override
            public void run() {
                minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
                Log.d("VV", String.valueOf(minBufSize));
                short[] buffer = new short[minBufSize];
                byte[] byteBuffer = new byte[minBufSize];
                String string = "minBufSize: " + minBufSize;
                Log.d("VV", string);
                String host = getIntent().getStringExtra("host");
                if (host.equals("localhost") || host.equals("")) {
                    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, minBufSize);
                    Log.d("VV", "Recorder initialized");
                    recorder.startRecording();
                    while (true) {
                        minBufSize = recorder.read(buffer, 0, buffer.length);
                        for (int i = 0; i < minBufSize; i++) {
                            byteBuffer[i] = (byte) (((buffer[i] + 32768) >> 8) & 0xff);
                        }
                        waveformView.setWaveform(byteBuffer);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
//                } else try {
//                    InetAddress destination = InetAddress.getByName(host);
//                    Socket socket = new Socket(destination, port);
//                    DataInputStream in = new DataInputStream(socket.getInputStream());
//                    while (true) {
//                        in.readFully(buffer, 0, minBufSize);
//                        waveformView.setWaveform(buffer);
//                    }
//                } catch (UnknownHostException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }
        });
        streamThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PermissionsChecker checker = new PermissionsChecker(this);

        if (checker.lacksPermissions(PERMISSIONS))
//        {
            startPermissionsActivity();
//        } else {
//            startVisualiser();
//        }
    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, PERMISSIONS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            finish();
        }
    }

    @Override
    protected void onPause() {
//        if (recorder != null) {
//            recorder.stop();
//        }
        super.onPause();
    }
}
