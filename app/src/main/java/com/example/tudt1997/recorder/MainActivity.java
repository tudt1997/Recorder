package com.example.tudt1997.recorder;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Button startButton;
    private Button stopButton;
    private int port = 50005;

    AudioRecord recorder;
    private int sampleRate = 16000;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private volatile boolean status;

    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = false;
        if (status)
            Log.d("VS", "Working");
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);

        startButton.setOnClickListener(startListener);
        stopButton.setOnClickListener(stopListener);

        context = getApplicationContext();


    }

    private final OnClickListener startListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!status) {
                CharSequence startText = "Recording";
                Toast startToast = Toast.makeText(context, startText, Toast.LENGTH_SHORT);
                startToast.show();

                status = true;
                startStreaming();
                Log.d("VS", "Starting");
            }
        }
    };

    private final OnClickListener stopListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (status) {
                CharSequence stopText = "Stopping";
                Toast stopToast = Toast.makeText(context, stopText, Toast.LENGTH_SHORT);
                stopToast.show();

                status = false;
                if (recorder != null) {
                    recorder.release();
                }
            }
            Log.d("VS", "Recorder released");
        }
    };

    public void onClickVisualize(View view) {
        Intent intent = new Intent(MainActivity.this, VisualizerActivity.class);
        EditText editText = findViewById(R.id.editText);
        String host = editText.getText().toString();
        intent.putExtra("host", host);
        this.startActivity(intent);
    }

    public void startStreaming() {
        Thread streamThread = new Thread(new Runnable() {
            @Override
            public void run() {
            try {

//                DatagramSocket socket = new DatagramSocket();
                Log.d("VS", "Socket Created");

                byte[] buffer = new byte[minBufSize];

                Log.d("VS", "Buffer created of size " + minBufSize);
//                DatagramPacket packet;

                EditText editText = findViewById(R.id.editText);
                String host = editText.getText().toString();
                InetAddress destination = InetAddress.getByName(host);
                Log.d("VS", "Address retrieved");

                Socket socketTCP = new Socket(destination, port);
                DataOutputStream out = new DataOutputStream(socketTCP.getOutputStream());
                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, minBufSize * 10);
                Log.d("VS", "Recorder initialized");

                recorder.startRecording();

                while (status) {
                    //reading data from MIC into buffer
                    minBufSize = recorder.read(buffer, 0, buffer.length);

                    //putting buffer in the packet
//                    packet = new DatagramPacket(buffer, buffer.length, destination, port);
//
//                    socket.send(packet);
                    out.write(buffer, 0, 1280);
                    System.out.println("MinBufferSize: " + minBufSize);
                }
            } catch (UnknownHostException e) {
                Log.e("VS", "UnknownHostException");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("VS", "IOException");
            }
            }

        });
        streamThread.start();
    }
}
