package com.dji.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import dji.sdk.keyvalue.key.CameraKey;
import dji.sdk.keyvalue.key.DJIKey;
import dji.sdk.keyvalue.key.FlightControllerKey;
import dji.sdk.keyvalue.key.GimbalKey;
import dji.sdk.keyvalue.key.KeyTools;
import dji.sdk.keyvalue.msdkkeyinfo.KeyCameraType;
import dji.sdk.keyvalue.value.camera.CameraExposureMode;
import dji.sdk.keyvalue.value.camera.CameraFocusMode;
import dji.sdk.keyvalue.value.camera.CameraISO;
import dji.sdk.keyvalue.value.camera.CameraMode;
import dji.sdk.keyvalue.value.camera.CameraShutterSpeed;
import dji.sdk.keyvalue.value.camera.CameraStorageInfos;
import dji.sdk.keyvalue.value.camera.CameraVideoStreamSourceType;
import dji.sdk.keyvalue.value.camera.GeneratedMediaFileInfo;
import dji.sdk.keyvalue.value.common.Attitude;
import dji.sdk.keyvalue.value.common.CameraLensType;
import dji.sdk.keyvalue.value.common.ComponentIndexType;
import dji.sdk.keyvalue.value.common.DoublePoint2D;
import dji.sdk.keyvalue.value.common.EmptyMsg;
import dji.sdk.keyvalue.value.common.Velocity3D;
import dji.sdk.keyvalue.value.flightcontroller.FlightControlAuthorityChangeReason;
import dji.sdk.keyvalue.value.flightcontroller.FlightCoordinateSystem;
import dji.sdk.keyvalue.value.flightcontroller.RollPitchControlMode;
import dji.sdk.keyvalue.value.flightcontroller.VerticalControlMode;
import dji.sdk.keyvalue.value.flightcontroller.VirtualStickFlightControlParam;
import dji.sdk.keyvalue.value.flightcontroller.YawControlMode;
import dji.sdk.keyvalue.value.gimbal.GimbalAngleRotation;
import dji.sdk.keyvalue.value.gimbal.GimbalAngleRotationMode;
import dji.sdk.keyvalue.value.gimbal.GimbalAttitudeRange;
import dji.sdk.keyvalue.value.gimbal.GimbalMode;
import dji.sdk.keyvalue.value.payload.WidgetType;
import dji.sdk.keyvalue.value.payload.WidgetValue;
import dji.v5.common.callback.CommonCallbacks;
import dji.v5.common.error.IDJIError;
import dji.v5.common.register.DJISDKInitEvent;
import dji.v5.common.video.channel.VideoChannelType;
import dji.v5.common.video.decoder.DecoderOutputMode;
import dji.v5.common.video.decoder.VideoDecoder;
import dji.v5.common.video.interfaces.IVideoChannel;
import dji.v5.common.video.stream.StreamSource;
import dji.v5.manager.KeyManager;
import dji.v5.manager.SDKManager;
import dji.v5.manager.aircraft.payload.PayloadCenter;
import dji.v5.manager.aircraft.payload.PayloadIndexType;
import dji.v5.manager.aircraft.payload.data.PayloadWidgetInfo;
import dji.v5.manager.aircraft.payload.listener.PayloadWidgetInfoListener;
import dji.v5.manager.aircraft.virtualstick.VirtualStickManager;
import dji.v5.manager.aircraft.virtualstick.VirtualStickState;
import dji.v5.manager.aircraft.virtualstick.VirtualStickStateListener;
import dji.v5.manager.datacenter.livestream.LiveStreamManager;
import dji.v5.manager.datacenter.livestream.LiveStreamSettings;
import dji.v5.manager.datacenter.livestream.LiveStreamStatus;
import dji.v5.manager.datacenter.livestream.LiveStreamStatusListener;
import dji.v5.manager.datacenter.livestream.LiveStreamType;
import dji.v5.manager.datacenter.livestream.settings.RtspSettings;
import dji.v5.manager.datacenter.media.MediaFile;
import dji.v5.manager.datacenter.media.MediaFileDownloadListener;
import dji.v5.manager.datacenter.media.MediaFileFilter;
import dji.v5.manager.datacenter.media.MediaFileListData;
import dji.v5.manager.datacenter.media.MediaFileListState;
import dji.v5.manager.datacenter.media.MediaFileListStateListener;
import dji.v5.manager.datacenter.media.MediaManager;
import dji.v5.manager.datacenter.media.PullMediaFileListParam;
import dji.v5.manager.datacenter.video.VideoStreamManager;
import dji.v5.manager.interfaces.ILiveStreamManager;
import dji.v5.manager.interfaces.IPayloadManager;
import dji.v5.manager.interfaces.SDKManagerCallback;
import dji.v5.utils.common.ContextUtil;
import dji.v5.utils.common.DiskUtil;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * author:chenjunzhu
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };

    private Button mBtnEnableVirtualStick;
    private Button mBtnDisableVirtualStick;
    private Button mBtnTakeOff;
    private Button mBtnLand;
    private Button mBtnTrack;
    private Button mBtnGimbal;
    private Button mBtnLive;
    private Button mBtnPhoto;
    private Button mBtnUpload;
    private OnScreenJoystick mScreenJoystickRight;
    private OnScreenJoystick mScreenJoystickLeft;
    private SurfaceView mSurfaceView;

    private Timer mSendVirtualStickDataTimer;
    private SendVirtualStickDataTask mSendVirtualStickDataTask;
    private boolean virtualFlag = false;
    // 跟随开启标志位
    private boolean trackOn = false;
    private boolean trackFlag = true;
    // 第一次开启跟随模式标志位
    private boolean firstTrack = true;


    private float mPitch;
    private float mRoll;
    private float mYaw;
    private float mThrottle;
    private float lidarX;
    private float lidarY;
    private float lidarZ;
    private double focusTargetX;
    private double focusTargetY;
    private double focusTargetX_;
    private double focusTargetY_;
    private int photoFlag = 0;
    private boolean photoUpFlag = false;

    private Attitude mAttitude = new Attitude(0.0, 0.0, 0.0);
    private double realYaw = 0.0;
    private String realYawStr = "0";
    private Velocity3D mVelocity = new Velocity3D(0.0, 0.0, 0.0);
    private double zVelocity = 0.0;
    private String zVelocityStr = "0";
    private double xVelocity = 0.0;
    private String xVelocityStr = "0";
    private double yVelocity = 0.0;
    private String yVelocityStr = "0";

    private String readText;
    private String readNum[];

    private String photoText;
    private String photoReadNum[];

    private boolean frameOn = false;
    private boolean firstFrame = true;
    private byte[] videoData;

    private boolean liveOn = false;
    private boolean firstLive = true;

    private boolean payloadOn = false;

    private int SDCapacity = 0;
    private int mediaIndex = 0;
    private int lastMediaIndex = 0;
    private boolean upFlag = false;
    private int canPhoto = 0;
    /*private Socket photoSocket;*/
    private boolean firstPhoto = true;
    private MediaFileListState realMediaFileListState;
    private boolean firstAvoid = true;
    private boolean uploadFinish = true;
    private boolean pullSuccess = false;
    private boolean firstGimbal = true;
    private boolean gimbalUpFlag = false;
    private int focusValue = 0;
    private int adjustFlag = 0;
    private int gimbalAdjustFlagFinished = 0;
    private int adjustFlagFinished = 0;
    private int landFlag = 0;
    private boolean landProcess = true;

    private long stime = 0;
    private long etime = 0;

    private List<String> missingPermission = new ArrayList<>();
    private static final int REQUEST_PERMISSION_CODE = 12345;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAndRequestPermissions();
        setContentView(R.layout.activity_main);
        registerApp();
        initUI();
    }

    /**
     * 每个触屏按键的功能
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            // 开启虚拟摇杆控制模式
            case R.id.btn_enable_virtual_stick:

                VirtualStickManager.getInstance().setVirtualStickStateListener(new VirtualStickStateListener() {
                    @Override
                    public void onVirtualStickStateUpdate(@NonNull VirtualStickState stickState) {

                    }

                    @Override
                    public void onChangeReasonUpdate(@NonNull FlightControlAuthorityChangeReason reason) {

                    }
                });

                VirtualStickManager.getInstance().enableVirtualStick(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        showToast("Enable Virtual Stick Success");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        showToast("Enable Virtual Stick Fail");
                    }
                });

                virtualFlag = true;

                break;

            // 关闭虚拟摇杆控制模式
            case R.id.btn_disable_virtual_stick:

                VirtualStickManager.getInstance().setVirtualStickStateListener(new VirtualStickStateListener() {
                    @Override
                    public void onVirtualStickStateUpdate(@NonNull VirtualStickState stickState) {

                    }

                    @Override
                    public void onChangeReasonUpdate(@NonNull FlightControlAuthorityChangeReason reason) {

                    }
                });

                VirtualStickManager.getInstance().disableVirtualStick(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        showToast("Disable Virtual Stick Success");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        showToast("Disable Virtual Stick Fail");
                    }
                });

                virtualFlag = false;

                break;

            // 起飞指令
            case R.id.btn_take_off:

                System.out.println("take off");

                KeyManager.getInstance().listen(KeyTools.createKey(FlightControllerKey.KeyConnection), this, new CommonCallbacks.KeyListener<Boolean>() {
                    @Override
                    public void onValueChange(@Nullable Boolean oldValue, @Nullable Boolean newValue) {

                    }
                });

                KeyManager.getInstance().performAction(KeyTools.createKey(FlightControllerKey.KeyStartTakeoff), new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                    @Override
                    public void onSuccess(EmptyMsg emptyMsg) {
                        showToast("Take off Success");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        showToast("Take off Fail");
                    }
                });

                break;

            // 降落指令
            case R.id.btn_land:

                System.out.println("land");

                KeyManager.getInstance().listen(KeyTools.createKey(FlightControllerKey.KeyConnection), this, new CommonCallbacks.KeyListener<Boolean>() {
                    @Override
                    public void onValueChange(@Nullable Boolean oldValue, @Nullable Boolean newValue) {

                    }
                });

                KeyManager.getInstance().performAction(KeyTools.createKey(FlightControllerKey.KeyStartAutoLanding), new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                    @Override
                    public void onSuccess(EmptyMsg emptyMsg) {
                        showToast("Auto Land Success");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        showToast("Auto Land Fail");
                    }
                });

                break;

            // 跟随控制程序
            case R.id.btn_track:

                showToast("Track");

                // 开启定时发送虚拟摇杆控制信息
                if (null == mSendVirtualStickDataTimer) {
                    mSendVirtualStickDataTask = new SendVirtualStickDataTask();
                    mSendVirtualStickDataTimer = new Timer();
                    mSendVirtualStickDataTimer.schedule(mSendVirtualStickDataTask, 100, 200);
                }

                KeyManager.getInstance().listen(KeyTools.createKey(FlightControllerKey.KeyConnection), this, new CommonCallbacks.KeyListener<Boolean>() {
                    @Override
                    public void onValueChange(@Nullable Boolean oldValue, @Nullable Boolean newValue) {

                    }
                });

                if(!trackOn) {
                    // 只有是第一次的时候才会创建一个线程去接收无人车的控制命令
                    if (firstTrack) {
                        // 创建一个线程去循环接收无人车的控制命令，只有第一次进入跟随模式时才会去创建线程
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    // 开启TCP客户端程序，接收无人车的命令
                                    Socket socket = new Socket("192.168.3.217", 12345);
                                    showToast("Connect Success");

                                    InputStream is = socket.getInputStream();
                                    OutputStream os = socket.getOutputStream();

                                    InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                                    OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");

                                    BufferedReader br = new BufferedReader(isr);
                                    BufferedWriter bw = new BufferedWriter(osw);

                                    try {
                                        Thread.currentThread().sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    // 设置文件保存路径
                                    File fileDir = new File(DiskUtil.getExternalCacheDirPath(ContextUtil.getContext(), "/FlightLog"));
                                    if(!fileDir.exists()) {
                                        fileDir.mkdirs();
                                    }

                                    while(true) {

                                        /*KeyManager.getInstance().getValue(KeyTools.createKey(FlightControllerKey.KeyAircraftAttitude), new CommonCallbacks.CompletionCallbackWithParam<Attitude>() {
                                            @Override
                                            public void onSuccess(Attitude attitude) {
                                                realYaw = attitude.getYaw();
                                                realYawStr = String.valueOf(realYaw);
                                            }

                                            @Override
                                            public void onFailure(@NonNull IDJIError error) {

                                            }
                                        });*/

                                        // 获取无人机的偏航信息，偏航需要与无人车的偏航对齐
                                        mAttitude = KeyManager.getInstance().getValue(KeyTools.createKey(FlightControllerKey.KeyAircraftAttitude));
                                        realYaw = mAttitude.getYaw();
                                        realYawStr = String.valueOf(realYaw);

                                        // 获取无人机的速度估计信息
                                        Velocity3D mVelocity = KeyManager.getInstance().getValue(KeyTools.createKey(FlightControllerKey.KeyAircraftVelocity));
                                        zVelocity = mVelocity.getZ();
                                        zVelocityStr = String.valueOf(zVelocity);
                                        xVelocity = mVelocity.getX();
                                        xVelocityStr = String.valueOf(xVelocity);
                                        yVelocity = mVelocity.getY();
                                        yVelocityStr = String.valueOf(yVelocity);

                                        // 发送无人机的偏航信息给无人车
                                        bw.write(realYawStr + "/" + gimbalAdjustFlagFinished + '\n');
                                        if (gimbalAdjustFlagFinished == 1) {
                                            gimbalAdjustFlagFinished = 0;
                                        }
                                        bw.flush();

                                        // 读取无人车的控制信息
                                        readText = br.readLine();
                                        /*System.out.println(readText);*/
                                        readNum = readText.split("/");

                                        mRoll = Float.parseFloat(readNum[0]);
                                        mYaw = Float.parseFloat(readNum[1]);
                                        mPitch = Float.parseFloat(readNum[2]);
                                        mThrottle = Float.parseFloat(readNum[3]);
                                        photoFlag = Integer.parseInt(readNum[4]);
                                        lidarX = Float.parseFloat(readNum[5]);
                                        lidarY = Float.parseFloat(readNum[6]);
                                        lidarZ = Float.parseFloat(readNum[7]);
                                        focusTargetX = Double.parseDouble(readNum[8]);
                                        focusTargetY = Double.parseDouble(readNum[9]);
                                        adjustFlag = Integer.parseInt(readNum[10]);
                                        adjustFlagFinished = Integer.parseInt(readNum[11]);
                                        landFlag = Integer.parseInt(readNum[12]);


                                        // 记录无人机的状态信息
                                        try {
                                            File logFile = new File(DiskUtil.getExternalCacheDirPath(ContextUtil.getContext(), "/FlightLog/flightlog.txt"));
                                            FileWriter fileWriter = new FileWriter(logFile, true);
                                            PrintWriter printWriter = new PrintWriter(fileWriter);
                                            Date logTime = new Date();
                                            SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                            String strTime = sdfTime.format(logTime);
                                            printWriter.println("Time " + strTime + " " + "photoFlag " + photoFlag + " " + "xVelocityStr " + xVelocityStr + " " + "yVelocityStr " + yVelocityStr + " " + "zVelocityStr " + zVelocityStr + " " + "lidarX " + lidarX + " " + "lidarY " + lidarY + " " + "lidarZ " + lidarZ+ " " + "Yaw " + realYawStr);
                                            printWriter.flush();
                                            fileWriter.flush();
                                        } catch (IOException ioException) {
                                            ioException.printStackTrace();
                                        }

                                        if (adjustFlag == 1) {
                                            // 设置云台角度Pitch和Yaw
                                            GimbalAngleRotation gimbalAngleRotation = new GimbalAngleRotation();
                                            gimbalAngleRotation.setMode(GimbalAngleRotationMode.ABSOLUTE_ANGLE);
                                            gimbalAngleRotation.setPitch(-90.0);
                                            gimbalAngleRotation.setYaw(0.0);

                                            KeyManager.getInstance().performAction(KeyTools.createKey(GimbalKey.KeyRotateByAngle), gimbalAngleRotation, new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                                                @Override
                                                public void onSuccess(EmptyMsg emptyMsg) {
                                                    gimbalAdjustFlagFinished = 1;
                                                }

                                                @Override
                                                public void onFailure(@NonNull IDJIError error) {

                                                }
                                            });
                                        }

                                        if (adjustFlagFinished == 1) {
                                            // 设置云台角度Pitch和Yaw
                                            GimbalAngleRotation gimbalAngleRotation = new GimbalAngleRotation();
                                            gimbalAngleRotation.setMode(GimbalAngleRotationMode.ABSOLUTE_ANGLE);
                                            gimbalAngleRotation.setPitch(0.0);
                                            gimbalAngleRotation.setYaw(0.0);

                                            KeyManager.getInstance().performAction(KeyTools.createKey(GimbalKey.KeyRotateByAngle), gimbalAngleRotation, new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                                                @Override
                                                public void onSuccess(EmptyMsg emptyMsg) {

                                                }

                                                @Override
                                                public void onFailure(@NonNull IDJIError error) {

                                                }
                                            });
                                        }

                                        //下降指令
                                        if (landFlag == 1 && landProcess) {
                                            landProcess = false;
                                            VirtualStickManager.getInstance().disableVirtualStick(new CommonCallbacks.CompletionCallback() {
                                                @Override
                                                public void onSuccess() {
                                                    showToast("Disable Virtual Stick Success");
                                                    KeyManager.getInstance().performAction(KeyTools.createKey(FlightControllerKey.KeyStartAutoLanding), new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                                                        @Override
                                                        public void onSuccess(EmptyMsg emptyMsg) {
                                                            showToast("Auto Land Success");
                                                        }

                                                        @Override
                                                        public void onFailure(@NonNull IDJIError error) {
                                                            showToast("Auto Land Fail");
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onFailure(@NonNull IDJIError error) {
                                                    showToast("Disable Virtual Stick Fail");
                                                }
                                            });
                                        }

                                        // 拍照标志位
                                        if(photoFlag == 1) {
                                            // 只有在上升沿时刻才会进行拍照
                                            if (!photoUpFlag) {
                                                try {
                                                    Thread.sleep(100);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }

                                                KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraMode), CameraMode.PHOTO_NORMAL, new CommonCallbacks.CompletionCallback() {
                                                    @Override
                                                    public void onSuccess() {
                                                        // 获取当前内存信息
                                                        KeyManager.getInstance().getValue(KeyTools.createKey(CameraKey.KeyCameraStorageInfos), new CommonCallbacks.CompletionCallbackWithParam<CameraStorageInfos>() {
                                                            @Override
                                                            public void onSuccess(CameraStorageInfos cameraStorageInfos) {
                                                                SDCapacity = cameraStorageInfos.getCurrentCameraStorageInfo().getStorageCapacity();
                                                                // 内存大于100MB时才会进行拍照
                                                                if (SDCapacity > 100) {
                                                                    KeyManager.getInstance().performAction(KeyTools.createKey(CameraKey.KeyStartShootPhoto), new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                                                                        @Override
                                                                        public void onSuccess(EmptyMsg emptyMsg) {
                                                                            showToast("Photo Success");
                                                                        }

                                                                        @Override
                                                                        public void onFailure(@NonNull IDJIError error) {
                                                                            showToast("Photo Fail");
                                                                        }
                                                                    });
                                                                }
                                                            }

                                                            @Override
                                                            public void onFailure(@NonNull IDJIError error) {

                                                            }
                                                        });

                                                    }

                                                    @Override
                                                    public void onFailure(@NonNull IDJIError error) {
                                                        showToast("set fail!!!");
                                                    }
                                                });

                                                // 设置对焦模式AFS
                                                /*KeyManager.getInstance().setValue(KeyTools.createCameraKey(CameraKey.KeyCameraFocusMode, ComponentIndexType.LEFT_OR_MAIN, CameraLensType.CAMERA_LENS_ZOOM), CameraFocusMode.AF, new CommonCallbacks.CompletionCallback() {
                                                    @Override
                                                    public void onSuccess() {
                                                        // 设置成功后进行对焦
                                                        *//*System.out.println("-----------对焦：CameraFocusMode.MANUAL设置成功-----------");*//*
                                                        DoublePoint2D targetPos = new DoublePoint2D(0.5, 0.5);
                                                        targetPos.setX(focusTargetX);
                                                        targetPos.setY(focusTargetY);
                                                        KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraFocusTarget), targetPos, new CommonCallbacks.CompletionCallback() {
                                                            @Override
                                                            public void onSuccess() {
                                                                // 设置对焦点成功后执行拍照动作
                                                                KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraMode), CameraMode.PHOTO_NORMAL, new CommonCallbacks.CompletionCallback() {
                                                                    @Override
                                                                    public void onSuccess() {
                                                                        // 获取当前内存信息
                                                                        KeyManager.getInstance().getValue(KeyTools.createKey(CameraKey.KeyCameraStorageInfos), new CommonCallbacks.CompletionCallbackWithParam<CameraStorageInfos>() {
                                                                            @Override
                                                                            public void onSuccess(CameraStorageInfos cameraStorageInfos) {
                                                                                SDCapacity = cameraStorageInfos.getCurrentCameraStorageInfo().getStorageCapacity();
                                                                                // 内存大于100MB时才会进行拍照
                                                                                if (SDCapacity > 100) {
                                                                                    KeyManager.getInstance().performAction(KeyTools.createKey(CameraKey.KeyStartShootPhoto), new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                                                                                        @Override
                                                                                        public void onSuccess(EmptyMsg emptyMsg) {
                                                                                            showToast("Photo Success");
                                                                                        }

                                                                                        @Override
                                                                                        public void onFailure(@NonNull IDJIError error) {
                                                                                            showToast("Photo Fail");
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onFailure(@NonNull IDJIError error) {

                                                                            }
                                                                        });

                                                                    }

                                                                    @Override
                                                                    public void onFailure(@NonNull IDJIError error) {
                                                                        showToast("set fail!!!");
                                                                    }
                                                                });
                                                            }

                                                            @Override
                                                            public void onFailure(@NonNull IDJIError error) {
                                                                *//*System.out.println("----------------设置对焦目标点失败----------------");*//*
                                                            }
                                                        });

                                                    }

                                                    @Override
                                                    public void onFailure(@NonNull IDJIError error) {
                                                        *//*System.out.println("-----------对焦：CameraFocusMode.MANUAL设置失败-----------");*//*
                                                    }
                                                });*/

                                                photoUpFlag = true;
                                            }
                                        } else {
                                            photoUpFlag = false;
                                        }

                                        try {
                                            Thread.currentThread().sleep(25);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                        firstTrack = false;
                    }
                    trackOn = true;
                } else {
                    trackOn = false;
                }

                break;

            // 云台控制
            case R.id.btn_gimbal:

                /*KeyManager.getInstance().setValue(KeyTools.createCameraKey(CameraKey.KeyCameraFocusMode, ComponentIndexType.LEFT_OR_MAIN, CameraLensType.CAMERA_LENS_ZOOM), CameraFocusMode.AFC, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        System.out.println("-----------对焦：CameraFocusMode.MANUAL设置成功-----------");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        System.out.println("-----------对焦：CameraFocusMode.MANUAL设置失败-----------");
                    }
                });*/

                // 设置云台角度Pitch和Yaw
                GimbalAngleRotation gimbalAngleRotation = new GimbalAngleRotation();
                gimbalAngleRotation.setMode(GimbalAngleRotationMode.ABSOLUTE_ANGLE);
                gimbalAngleRotation.setPitch(0.0);
                gimbalAngleRotation.setYaw(0.0);

                KeyManager.getInstance().performAction(KeyTools.createKey(GimbalKey.KeyRotateByAngle), gimbalAngleRotation, new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                    @Override
                    public void onSuccess(EmptyMsg emptyMsg) {

                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {

                    }
                });

                /*List<StreamSource> frame = VideoStreamManager.getInstance().getAvailableStreamSources();
                IVideoChannel availableVideoChannel = VideoStreamManager.getInstance().getAvailableVideoChannel(VideoChannelType.PRIMARY_STREAM_CHANNEL);

                if (frame != null) {
                    if (!frameOn) {
                        availableVideoChannel.startChannel(frame.get(0), new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onSuccess() {
                                showToast("Get Video Success");
                            }

                            @Override
                            public void onFailure(@NonNull IDJIError error) {
                                showToast("Get Video Fail");
                            }
                        });
                        frameOn = true;

                        availableVideoChannel.addStreamDataListener(new StreamDataListener() {
                            @Override
                            public void onReceive(IVideoFrame videoFrame) {
                                videoData = videoFrame.getData();
                            }
                        });


                        if (firstFrame) {
                            VideoDecoder videoDecoder = new VideoDecoder(this.getBaseContext(), availableVideoChannel.getVideoChannelType(), DecoderOutputMode.SURFACE_MODE, mSurfaceView);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        Socket socketStream = new Socket("192.168.3.206", 12345);
                                        showToast("Connect Success");

                                        OutputStream osStream = socketStream.getOutputStream();

                                        OutputStreamWriter oswStream = new OutputStreamWriter(osStream, "UTF-8");

                                        BufferedWriter bwSteam = new BufferedWriter(oswStream);

                                        try {
                                            Thread.currentThread().sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        while(true) {
                                            bwSteam.write(String.valueOf(videoData.length) + ",0");
                                            bwSteam.write(new String(videoData) + '\n');
                                            bwSteam.flush();

                                            try {
                                                Thread.currentThread().sleep(25);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }

                                        }

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }).start();

                            firstFrame = false;
                        }

                    } else {
                        availableVideoChannel.closeChannel(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onSuccess() {
                                showToast("Close Video Success");
                            }

                            @Override
                            public void onFailure(@NonNull IDJIError error) {
                                showToast("Close Video Fail");
                            }
                        });
                        frameOn = false;
                    }
                } else {
                    showToast("Get Video Fail");
                }
                showToast("Get Video Fail");

                showToast("Gimbal");
                KeyManager.getInstance().listen(KeyTools.createKey(GimbalKey.KeyConnection), this, new CommonCallbacks.KeyListener<Boolean>() {
                    @Override
                    public void onValueChange(@Nullable Boolean oldValue, @Nullable Boolean newValue) {

                    }
                });

                KeyManager.getInstance().getValue(KeyTools.createKey(GimbalKey.KeyGimbalAttitude), new CommonCallbacks.CompletionCallbackWithParam<Attitude>() {
                    @Override
                    public void onSuccess(Attitude attitude) {

                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {

                    }
                });

                KeyManager.getInstance().getValue(KeyTools.createKey(GimbalKey.KeyGimbalAttitudeRange), new CommonCallbacks.CompletionCallbackWithParam<GimbalAttitudeRange>() {
                    @Override
                    public void onSuccess(GimbalAttitudeRange gimbalAttitudeRange) {

                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {

                    }
                });

                KeyManager.getInstance().getValue(KeyTools.createKey(GimbalKey.KeyGimbalMode), new CommonCallbacks.CompletionCallbackWithParam<GimbalMode>() {
                    @Override
                    public void onSuccess(GimbalMode gimbalMode) {

                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {

                    }
                });

                GimbalAngleRotation gimbalAngleRotation = new GimbalAngleRotation();
                gimbalAngleRotation.setMode(GimbalAngleRotationMode.RELATIVE_ANGLE);
                gimbalAngleRotation.setPitch(0.0);
                gimbalAngleRotation.setYaw(0.0);

                KeyManager.getInstance().performAction(KeyTools.createKey(GimbalKey.KeyRotateByAngle), gimbalAngleRotation, new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                    @Override
                    public void onSuccess(EmptyMsg emptyMsg) {

                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {

                    }
                });

                if (firstGimbal) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Socket gimbalSocket = new Socket("192.168.0.202", 8086);
                                showToast("Connect Success");
                                InputStream gimbalIs = gimbalSocket.getInputStream();
                                InputStreamReader gimbalIsr = new InputStreamReader(gimbalIs, "UTF-8");
                                BufferedReader gimbalBr = new BufferedReader(gimbalIsr);
                                while(true) {
                                    String gimbalText = gimbalBr.readLine();
                                    System.out.println(gimbalText);
                                    String[] gimbalPitchText = gimbalText.split("/");
                                    if (gimbalPitchText[0].equals("2")) {
                                        if (!gimbalUpFlag) {
                                            double gimbalPitch = (0.5 - Double.parseDouble(gimbalPitchText[1])) * 40;
                                            if (gimbalPitch < -90) {
                                                gimbalPitch = -90;
                                            }
                                            if (gimbalPitch > 35) {
                                                gimbalPitch = 35;
                                            }
                                            gimbalAngleRotation.setPitch(gimbalPitch);
                                            KeyManager.getInstance().performAction(KeyTools.createKey(GimbalKey.KeyRotateByAngle), gimbalAngleRotation, new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                                                @Override
                                                public void onSuccess(EmptyMsg emptyMsg) {

                                                }

                                                @Override
                                                public void onFailure(@NonNull IDJIError error) {

                                                }
                                            });
                                            gimbalUpFlag = true;
                                        }
                                    } else {
                                        gimbalUpFlag = false;
                                    }

                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }*/

                break;

            // RTSP视频流
            case R.id.btn_start_live:

                // 获取可用的码流源
                List<StreamSource> frameLive = VideoStreamManager.getInstance().getAvailableStreamSources();
                // 获取可用的码流通道
                IVideoChannel availableVideoChannelLive = VideoStreamManager.getInstance().getAvailableVideoChannel(VideoChannelType.PRIMARY_STREAM_CHANNEL);

                ILiveStreamManager iLiveStreamManager = LiveStreamManager.getInstance();

                if (frameLive != null) {

                    if (!liveOn) {
                        // 绑定视频源并开启码流通道
                        availableVideoChannelLive.startChannel(frameLive.get(0), new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFailure(@NonNull IDJIError error) {

                            }
                        });

                        if (firstLive) {
                            firstLive = false;
                            // 设置解码方式并将图像渲染到屏幕上
                            VideoDecoder videoDecoderLive = new VideoDecoder(this.getBaseContext(), availableVideoChannelLive.getVideoChannelType(), DecoderOutputMode.SURFACE_MODE, mSurfaceView, 1080, 720, true);
                        }

                        // 设置直播状态监听器
                        iLiveStreamManager.addLiveStreamStatusListener(new LiveStreamStatusListener() {
                            @Override
                            public void onLiveStreamStatusUpdate(LiveStreamStatus status) {

                            }

                            @Override
                            public void onError(IDJIError error) {
                                showToast("LiveStream Fail");
                            }
                        });

                        // RTSP视频流的用户名，密码和端口
                        RtspSettings.Builder rtspSettingsBuilder = new RtspSettings.Builder();
                        rtspSettingsBuilder.setUserName("yijia");
                        rtspSettingsBuilder.setPassWord("123");
                        rtspSettingsBuilder.setPort(8554);
                        RtspSettings rtspSettings = rtspSettingsBuilder.build();

                        LiveStreamSettings.Builder liveStreamSettingsBuilder = new LiveStreamSettings.Builder();
                        liveStreamSettingsBuilder.setLiveStreamType(LiveStreamType.RTSP);
                        liveStreamSettingsBuilder.setRtspSettings(rtspSettings);

                        LiveStreamSettings liveStreamSettings = liveStreamSettingsBuilder.build();
                        iLiveStreamManager.setLiveStreamSettings(liveStreamSettings);

                        // 开启直播推流
                        iLiveStreamManager.startStream(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onSuccess() {
                                showToast("Start Live Success");
                            }

                            @Override
                            public void onFailure(@NonNull IDJIError error) {
                                showToast("Start Live Fail");
                            }
                        });
                        /*iLiveStreamManager.setVideoChannelType(VideoChannelType.PRIMARY_STREAM_CHANNEL);
                        iLiveStreamManager.setLiveStreamQuality(StreamQuality.FULL_HD);
                        iLiveStreamManager.setLiveVideoBitrateMode(LiveVideoBitrateMode.AUTO);
                        iLiveStreamManager.setLiveVideoBitrate(9600);*/
                        liveOn = true;
                    } else {
                        iLiveStreamManager.stopStream(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onSuccess() {
                                showToast("Stop Live Success");
                            }

                            @Override
                            public void onFailure(@NonNull IDJIError error) {
                                showToast("Stop Live Fail");
                            }
                        });
                        availableVideoChannelLive.closeChannel(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFailure(@NonNull IDJIError error) {

                            }
                        });
                        liveOn = false;
                    }
                } else {
                    showToast("LiveStream Fail");
                }

                break;

            // 单独拍照测试程序
            case R.id.btn_photo:

                if (!payloadOn) {
                    IPayloadManager payloadManager = PayloadCenter.getInstance().getPayloadManager().get(PayloadIndexType.UP);
                    payloadManager.addPayloadWidgetInfoListener(new PayloadWidgetInfoListener() {
                        @Override
                        public void onPayloadWidgetInfoUpdate(PayloadWidgetInfo info) {

                        }
                    });
                    payloadManager.pullWidgetInfoFromPayload(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailure(@NonNull IDJIError error) {

                        }
                    });
                    payloadManager.setWidgetValue(new WidgetValue(WidgetType.SWITCH, 0, 1), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onSuccess() {
                            payloadManager.setWidgetValue(new WidgetValue(WidgetType.RANGE, 1, 99), new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onFailure(@NonNull IDJIError error) {

                                }
                            });
                        }

                        @Override
                        public void onFailure(@NonNull IDJIError error) {

                        }
                    });
                    payloadOn = true;
                } else {
                    IPayloadManager payloadManager = PayloadCenter.getInstance().getPayloadManager().get(PayloadIndexType.UP);
                    payloadManager.addPayloadWidgetInfoListener(new PayloadWidgetInfoListener() {
                        @Override
                        public void onPayloadWidgetInfoUpdate(PayloadWidgetInfo info) {

                        }
                    });
                    payloadManager.pullWidgetInfoFromPayload(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailure(@NonNull IDJIError error) {

                        }
                    });
                    payloadManager.setWidgetValue(new WidgetValue(WidgetType.SWITCH, 0, 0), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailure(@NonNull IDJIError error) {

                        }
                    });
                    payloadOn = false;
                }

                //ISO参数调节
                KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyExposureMode), CameraExposureMode.MANUAL, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        System.out.println("-----------ISO:CameraExposureMode.MANUAL设置成功-----------");
                        KeyManager.getInstance().setValue(KeyTools.createCameraKey(CameraKey.KeyISO, ComponentIndexType.LEFT_OR_MAIN, CameraLensType.CAMERA_LENS_ZOOM), CameraISO.ISO_200, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onSuccess() {
                                System.out.println("-----------ISO:CameraISO.ISO_100设置成功-----------");
                            }

                            @Override
                            public void onFailure(@NonNull IDJIError error) {
                                System.out.println("-----------ISO:CameraISO.ISO_100设置失败-----------");
                            }
                        });
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        System.out.println("-----------ISO:CameraExposureMode.MANUAL设置失败-----------");
                    }
                });

                //快门
                KeyManager.getInstance().setValue(KeyTools.createCameraKey(CameraKey.KeyShutterSpeed, ComponentIndexType.LEFT_OR_MAIN, CameraLensType.CAMERA_LENS_ZOOM), CameraShutterSpeed.SHUTTER_SPEED1_1000, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        System.out.println("-----------快门：CameraShutterSpeed.SHUTTER_SPEED1_500设置成功-----------");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        System.out.println("-----------快门：CameraShutterSpeed.SHUTTER_SPEED1_500设置失败-----------");
                    }
                });

                KeyManager.getInstance().getValue(KeyTools.createKey(CameraKey.KeyCameraFocusRingMinValue), new CommonCallbacks.CompletionCallbackWithParam<Integer>() {
                    @Override
                    public void onSuccess(Integer integer) {
                        System.out.println("--------------KeyCameraFocusRingMinValue: " + integer.intValue());
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {

                    }
                });
                KeyManager.getInstance().getValue(KeyTools.createKey(CameraKey.KeyCameraFocusRingMaxValue), new CommonCallbacks.CompletionCallbackWithParam<Integer>() {
                    @Override
                    public void onSuccess(Integer integer) {
                        focusValue = integer.intValue();
                        System.out.println("--------------KeyCameraFocusRingMaxValue: " + focusValue);
                        KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraVideoStreamSource), CameraVideoStreamSourceType.ZOOM_CAMERA, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onSuccess() {
                                KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraZoomRatios), 1.0, new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onSuccess() {
                                        KeyManager.getInstance().setValue(KeyTools.createCameraKey(CameraKey.KeyCameraFocusMode, ComponentIndexType.LEFT_OR_MAIN, CameraLensType.CAMERA_LENS_ZOOM), CameraFocusMode.MANUAL, new CommonCallbacks.CompletionCallback() {
                                            @Override
                                            public void onSuccess() {
                                                KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraFocusRingValue), focusValue, new CommonCallbacks.CompletionCallback() {
                                                    @Override
                                                    public void onSuccess() {
                                                        showToast("Set focusValue Success");
                                                    }

                                                    @Override
                                                    public void onFailure(@NonNull IDJIError error) {

                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFailure(@NonNull IDJIError error) {

                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(@NonNull IDJIError error) {

                                    }
                                });

                            }

                            @Override
                            public void onFailure(@NonNull IDJIError error) {

                            }
                        });
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {

                    }
                });

                // 建立与识别电脑的TCP连接，用于单独拍照，流程与Track中的流程一样
                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ServerSocket photoCam = new ServerSocket(8001);
                            Socket photoSocket = photoCam.accept();
                            showToast("Connect Success");
                            InputStream photoIs = photoSocket.getInputStream();
                            BufferedReader photoBr = new BufferedReader(new InputStreamReader(photoIs));
                            while (true) {
                                String photoMessage = photoBr.readLine();
                                System.out.println(photoMessage);
                                photoReadNum = photoMessage.split("/");
                                photoFlag = Integer.parseInt(photoReadNum[0]);
                                focusTargetX_ = Double.parseDouble(photoReadNum[1]);
                                focusTargetY_ = Double.parseDouble(photoReadNum[2]);
                                if(photoFlag == 2) {
                                    System.out.println("进来了！！！！");
                                    if (!photoUpFlag) {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        KeyManager.getInstance().setValue(KeyTools.createCameraKey(CameraKey.KeyCameraFocusMode, ComponentIndexType.LEFT_OR_MAIN, CameraLensType.CAMERA_LENS_ZOOM), CameraFocusMode.AF, new CommonCallbacks.CompletionCallback() {
                                            @Override
                                            public void onSuccess() {
                                                System.out.println("-----------对焦：CameraFocusMode.MANUAL设置成功-----------");
                                                DoublePoint2D targetPos = new DoublePoint2D(0.5, 0.5);
                                                targetPos.setX(focusTargetX_);
                                                targetPos.setY(focusTargetY_);
                                                KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraFocusTarget), targetPos, new CommonCallbacks.CompletionCallback() {
                                                    @Override
                                                    public void onSuccess() {
                                                        System.out.println("----------------设置对焦目标点成功----------------");
                                                        KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraMode), CameraMode.PHOTO_NORMAL, new CommonCallbacks.CompletionCallback() {
                                                            @Override
                                                            public void onSuccess() {
                                                                KeyManager.getInstance().getValue(KeyTools.createKey(CameraKey.KeyCameraStorageInfos), new CommonCallbacks.CompletionCallbackWithParam<CameraStorageInfos>() {
                                                                    @Override
                                                                    public void onSuccess(CameraStorageInfos cameraStorageInfos) {
                                                                        SDCapacity = cameraStorageInfos.getCurrentCameraStorageInfo().getStorageCapacity();
                                                                        if (SDCapacity > 100) {
                                                                            KeyManager.getInstance().performAction(KeyTools.createKey(CameraKey.KeyStartShootPhoto), new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                                                                                @Override
                                                                                public void onSuccess(EmptyMsg emptyMsg) {
                                                                                    showToast("Photo Success");
                                                                                }

                                                                                @Override
                                                                                public void onFailure(@NonNull IDJIError error) {
                                                                                    showToast("Photo Fail");
                                                                                }
                                                                            });
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onFailure(@NonNull IDJIError error) {

                                                                    }
                                                                });

                                                            }

                                                            @Override
                                                            public void onFailure(@NonNull IDJIError error) {
                                                                showToast("set fail!!!");
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onFailure(@NonNull IDJIError error) {
                                                        System.out.println("----------------设置对焦目标点失败----------------");
                                                    }
                                                });

                                            }

                                            @Override
                                            public void onFailure(@NonNull IDJIError error) {
                                                System.out.println("-----------对焦：CameraFocusMode.MANUAL设置失败-----------");
                                            }
                                        });

                                        photoUpFlag = true;
                                    }
                                } else {
                                    photoUpFlag = false;
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();*/

                // ISO参数调节
                /*KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyExposureMode), CameraExposureMode.MANUAL, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        *//*System.out.println("-----------ISO:CameraExposureMode.MANUAL设置成功-----------");*//*
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        *//*System.out.println("-----------ISO:CameraExposureMode.MANUAL设置失败-----------");*//*
                    }
                });
                KeyManager.getInstance().setValue(KeyTools.createCameraKey(CameraKey.KeyISO, ComponentIndexType.LEFT_OR_MAIN, CameraLensType.CAMERA_LENS_ZOOM), CameraISO.ISO_400, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        *//*System.out.println("-----------ISO:CameraISO.ISO_50设置成功-----------");*//*
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        *//*System.out.println("-----------ISO:CameraISO.ISO_50设置失败-----------");*//*
                    }
                });*/

                // 快门
                /*KeyManager.getInstance().setValue(KeyTools.createCameraKey(CameraKey.KeyShutterSpeed, ComponentIndexType.LEFT_OR_MAIN, CameraLensType.CAMERA_LENS_ZOOM), CameraShutterSpeed.SHUTTER_SPEED1_200, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        *//*System.out.println("-----------快门：CameraShutterSpeed.SHUTTER_SPEED1_1DOT25设置成功-----------");*//*
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        *//*System.out.println("-----------快门：CameraShutterSpeed.SHUTTER_SPEED1_1DOT25设置失败-----------");*//*
                    }
                });*/

                // 对焦
                /*KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraVideoStreamSource), CameraVideoStreamSourceType.ZOOM_CAMERA, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        *//*System.out.println("-----------对焦：CameraVideoStreamSourceType.ZOOM_CAMERA设置成功-----------");*//*
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        *//*System.out.println("-----------对焦：CameraVideoStreamSourceType.ZOOM_CAMERA设置失败-----------");*//*
                    }
                });
                KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraZoomRatios), 1.0, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {

                    }
                });*/
                /*KeyManager.getInstance().setValue(KeyTools.createCameraKey(CameraKey.KeyCameraFocusMode, ComponentIndexType.LEFT_OR_MAIN, CameraLensType.CAMERA_LENS_ZOOM), CameraFocusMode.AF, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        *//*System.out.println("-----------对焦：CameraFocusMode.MANUAL设置成功-----------");*//*
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        *//*System.out.println("-----------对焦：CameraFocusMode.MANUAL设置失败-----------");*//*
                    }
                });
                DoublePoint2D targetPos = new DoublePoint2D(0.5, 0.5);
                targetPos.setX(0.5);
                targetPos.setY(0.5);
                KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraFocusTarget), targetPos, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        System.out.println("----------------设置对焦目标点成功----------------");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        System.out.println("----------------设置对焦目标点失败----------------");
                    }
                });*/
                /*KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraFocusRingValue), 10, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        *//*System.out.println("-----------对焦：CameraKey.KeyCameraFocusRingValue设置成功-----------");*//*
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        *//*System.out.println("-----------对焦：CameraKey.KeyCameraFocusRingValue设置失败-----------");*//*
                    }
                });*/

                // 拍照
               /* KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraMode), CameraMode.PHOTO_NORMAL, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        KeyManager.getInstance().getValue(KeyTools.createKey(CameraKey.KeyCameraStorageInfos), new CommonCallbacks.CompletionCallbackWithParam<CameraStorageInfos>() {
                            @Override
                            public void onSuccess(CameraStorageInfos cameraStorageInfos) {
                                SDCapacity = cameraStorageInfos.getCurrentCameraStorageInfo().getStorageCapacity();
                                if (SDCapacity > 100) {
                                    KeyManager.getInstance().performAction(KeyTools.createKey(CameraKey.KeyStartShootPhoto), new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                                        @Override
                                        public void onSuccess(EmptyMsg emptyMsg) {
                                            showToast("Photo Success");
                                        }

                                        @Override
                                        public void onFailure(@NonNull IDJIError error) {
                                            showToast("Photo Fail");
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onFailure(@NonNull IDJIError error) {

                            }
                        });

                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {
                        showToast("set fail!!!");
                    }
                });*/

                /*GimbalAngleRotation gimbalAngleRotationPhoto = new GimbalAngleRotation();
                gimbalAngleRotationPhoto.setMode(GimbalAngleRotationMode.RELATIVE_ANGLE);
                gimbalAngleRotationPhoto.setPitch(0.0);
                gimbalAngleRotationPhoto.setYaw(0.0);

                KeyManager.getInstance().performAction(KeyTools.createKey(GimbalKey.KeyRotateByAngle), gimbalAngleRotationPhoto, new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                    @Override
                    public void onSuccess(EmptyMsg emptyMsg) {

                    }

                    @Override
                    public void onFailure(@NonNull IDJIError error) {

                    }
                });
                if (firstPhoto) {
                    KeyManager.getInstance().listen(KeyTools.createKey(CameraKey.KeyConnection), this, new CommonCallbacks.KeyListener<Boolean>() {
                        @Override
                        public void onValueChange(@Nullable Boolean oldValue, @Nullable Boolean newValue) {

                        }
                    });

                    KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraVideoStreamSource), CameraVideoStreamSourceType.ZOOM_CAMERA, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailure(@NonNull IDJIError error) {

                        }
                    });

                    KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraZoomRatios), 1.0, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailure(@NonNull IDJIError error) {

                        }
                    });


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                photoSocket = new Socket("192.168.3.202", 8086);
                                showToast("Connect Success");
                                InputStream photoIs = photoSocket.getInputStream();
                                InputStreamReader photoIsr = new InputStreamReader(photoIs, "UTF-8");
                                BufferedReader photoBr = new BufferedReader(photoIsr);

                                long startTime = System.currentTimeMillis();

                                while(true) {
                                    String photoFlag = photoBr.readLine();
                                    String[] photoText = photoFlag.split("/");
                                    System.out.println(photoFlag);
                                    if (photoText[0] != null && photoText[0].equals("2")) {
                                        long spendTime = System.currentTimeMillis() - startTime;
                                        if (!upFlag && spendTime > 3000) {
                                            startTime = System.currentTimeMillis();
                                            double pitchAdj = (0.5 - Double.parseDouble(photoText[1])) * 30;
                                            gimbalAngleRotationPhoto.setPitch(pitchAdj);

                                            KeyManager.getInstance().performAction(KeyTools.createKey(GimbalKey.KeyRotateByAngle), gimbalAngleRotationPhoto, new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                                                @Override
                                                public void onSuccess(EmptyMsg emptyMsg) {
                                                    KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraZoomRatios), 2.0, new CommonCallbacks.CompletionCallback() {
                                                        @Override
                                                        public void onSuccess() {
                                                            KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraMode), CameraMode.PHOTO_NORMAL, new CommonCallbacks.CompletionCallback() {
                                                                @Override
                                                                public void onSuccess() {
                                                                    KeyManager.getInstance().getValue(KeyTools.createKey(CameraKey.KeyCameraStorageInfos), new CommonCallbacks.CompletionCallbackWithParam<CameraStorageInfos>() {
                                                                        @Override
                                                                        public void onSuccess(CameraStorageInfos cameraStorageInfos) {
                                                                            SDCapacity = cameraStorageInfos.getCurrentCameraStorageInfo().getStorageCapacity();
                                                                            if (SDCapacity > 100) {
                                                                                KeyManager.getInstance().performAction(KeyTools.createKey(CameraKey.KeyStartShootPhoto), new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                                                                                    @Override
                                                                                    public void onSuccess(EmptyMsg emptyMsg) {
                                                                                        showToast("Photo Success");
                                                                                    }

                                                                                    @Override
                                                                                    public void onFailure(@NonNull IDJIError error) {
                                                                                        showToast("Photo Fail");
                                                                                    }
                                                                                });
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onFailure(@NonNull IDJIError error) {

                                                                        }
                                                                    });

                                                                }

                                                                @Override
                                                                public void onFailure(@NonNull IDJIError error) {
                                                                    showToast("set fail!!!");
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onFailure(@NonNull IDJIError error) {

                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onFailure(@NonNull IDJIError error) {

                                                }
                                            });
                                            upFlag = true;
                                        }
                                    } else {
                                        upFlag = false;
                                        KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraZoomRatios), 1.0, new CommonCallbacks.CompletionCallback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onFailure(@NonNull IDJIError error) {

                                            }
                                        });
                                    }
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    firstPhoto = false;
                }*/

                break;

            // 拉取图片并下载
            case R.id.btn_upload:

                showToast("Upload");
                // 添加媒体文件列表监听
                MediaManager.getInstance().addMediaFileListStateListener(new MediaFileListStateListener() {
                    @Override
                    public void onUpdate(MediaFileListState mediaFileListState) {
                        realMediaFileListState = mediaFileListState;
                    }
                });

                // 设置文件保存路径
                File fileDir = new File(DiskUtil.getExternalCacheDirPath(ContextUtil.getContext(), "/mediafile"));
                if(!fileDir.exists()) {
                    fileDir.mkdirs();
                }

                // 监听是否有新文件产生
                KeyManager.getInstance().listen(KeyTools.createKey(CameraKey.KeyNewlyGeneratedMediaFile), this, new CommonCallbacks.KeyListener<GeneratedMediaFileInfo>() {
                    @Override
                    public void onValueChange(@Nullable GeneratedMediaFileInfo oldValue, @Nullable GeneratedMediaFileInfo newValue) {
                        if(newValue != null) {
                            mediaIndex = newValue.getIndex();
                            MediaManager.getInstance().enable(new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onSuccess() {
                                    stime = System.currentTimeMillis();
                                    MediaManager.getInstance().pullMediaFileListFromCamera(new PullMediaFileListParam.Builder().filter(MediaFileFilter.PHOTO).build(), new CommonCallbacks.CompletionCallback() {
                                        @Override
                                        public void onSuccess() {
                                            showToast("Pull Success");
                                            pullSuccess = true;
                                        }

                                        @Override
                                        public void onFailure(@NonNull IDJIError error) {
                                            showToast("Pull Fail");
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(@NonNull IDJIError error) {
                                    System.out.println("Enable Fail");
                                }
                            });
                        }
                    }
                });

                // 单独开一个线程根据监听的标志去下载图片到遥控器
                if (firstAvoid) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            while(true) {
                                if(mediaIndex != lastMediaIndex && pullSuccess == true) {
                                    lastMediaIndex = mediaIndex;
                                    pullSuccess = false;
                                    while(realMediaFileListState != MediaFileListState.UP_TO_DATE) {
                                        /*System.out.println("-----------" + realMediaFileListState + "-----------");*/
                                    }
                                    etime = System.currentTimeMillis();
                                    long wasteTime = etime - stime;
                                    System.out.println("-----------拉取图片耗时： " + wasteTime + "ms");

                                    if (realMediaFileListState == MediaFileListState.UP_TO_DATE) {
                                        MediaFileListData mediaFileListData = MediaManager.getInstance().getMediaFileListData();
                                        List<MediaFile> photoData = mediaFileListData.getData();
                                        if (photoData.size() > 0) {
                                            /*System.out.println("----------" + "size: " + photoData.size() + "----------");*/
                                            MediaFile mediaFile = photoData.get(photoData.size() - 1);
                                            String externalCacheDirPath = DiskUtil.getExternalCacheDirPath(ContextUtil.getContext(), "/mediafile" + "/" + String.valueOf(photoData.size()) + ".jpg");
                                            try {
                                                FileOutputStream fos = new FileOutputStream(new File(externalCacheDirPath));
                                                mediaFile.pullOriginalMediaFileFromCamera(0, new MediaFileDownloadListener() {
                                                    @Override
                                                    public void onStart() {

                                                    }

                                                    @Override
                                                    public void onProgress(long total, long current) {
                                                        System.out.println("----------" + "总长度：" + total + " 当前长度： " + current + "----------");
                                                    }

                                                    @Override
                                                    public void onRealtimeDataUpdate(byte[] data, long position) {
                                                        uploadFinish = false;
                                                        try {
                                                            fos.write(data, 0, data.length);
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }

                                                    @Override
                                                    public void onFinish() {
                                                        uploadFinish = true;
                                                        MediaManager.getInstance().disable(new CommonCallbacks.CompletionCallback() {
                                                            @Override
                                                            public void onSuccess() {

                                                            }

                                                            @Override
                                                            public void onFailure(@NonNull IDJIError error) {

                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onFailure(IDJIError error) {

                                                    }
                                                });
                                            } catch (FileNotFoundException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            if (uploadFinish) {
                                                MediaManager.getInstance().disable(new CommonCallbacks.CompletionCallback() {
                                                    @Override
                                                    public void onSuccess() {

                                                    }

                                                    @Override
                                                    public void onFailure(@NonNull IDJIError error) {

                                                    }
                                                });
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }).start();
                    firstAvoid = false;
                }

                break;
        }
    }

    private void initUI() {
        mBtnEnableVirtualStick = (Button) findViewById(R.id.btn_enable_virtual_stick);
        mBtnDisableVirtualStick = (Button) findViewById(R.id.btn_disable_virtual_stick);
        mBtnTakeOff = (Button) findViewById(R.id.btn_take_off);
        mBtnLand = (Button) findViewById(R.id.btn_land);
        mBtnTrack = (Button) findViewById(R.id.btn_track);
        mBtnGimbal = (Button) findViewById(R.id.btn_gimbal);
        mBtnLive = (Button) findViewById(R.id.btn_start_live);
        mBtnPhoto = (Button) findViewById(R.id.btn_photo);
        mBtnUpload = (Button) findViewById(R.id.btn_upload);
        mSurfaceView = (SurfaceView) findViewById(R.id.btn_surfaceView);
        mBtnEnableVirtualStick.setOnClickListener(this);
        mBtnDisableVirtualStick.setOnClickListener(this);
        mBtnTakeOff.setOnClickListener(this);
        mBtnLand.setOnClickListener(this);
        mBtnTrack.setOnClickListener(this);
        mBtnGimbal.setOnClickListener(this);
        mBtnLive.setOnClickListener(this);
        mBtnPhoto.setOnClickListener(this);
        mBtnUpload.setOnClickListener(this);

        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {

            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

            }
        });
        mScreenJoystickRight = (OnScreenJoystick)findViewById(R.id.directionJoystickRight);
        mScreenJoystickLeft = (OnScreenJoystick)findViewById(R.id.directionJoystickLeft);

        mScreenJoystickRight.setJoystickListener(new OnScreenJoystickListener(){

            @Override
            public void onTouch(OnScreenJoystick joystick, float pX, float pY) {
                if(Math.abs(pX) < 0.02 ){
                    pX = 0;
                }

                if(Math.abs(pY) < 0.02 ){
                    pY = 0;
                }

                float pitchJoyControlMaxSpeed = 10;
                float rollJoyControlMaxSpeed = 10;

                mRoll = (float)(pitchJoyControlMaxSpeed * pX);
                mPitch = (float)(rollJoyControlMaxSpeed * pY);

                if (null == mSendVirtualStickDataTimer) {
                    mSendVirtualStickDataTask = new SendVirtualStickDataTask();
                    mSendVirtualStickDataTimer = new Timer();
                    mSendVirtualStickDataTimer.schedule(mSendVirtualStickDataTask, 100, 200);
                }

            }

        });

        mScreenJoystickLeft.setJoystickListener(new OnScreenJoystickListener() {

            @Override
            public void onTouch(OnScreenJoystick joystick, float pX, float pY) {
                if(Math.abs(pX) < 0.02 ){
                    pX = 0;
                }

                if(Math.abs(pY) < 0.02 ){
                    pY = 0;
                }
                float verticalJoyControlMaxSpeed = 30;
                float yawJoyControlMaxSpeed = 30;

                mYaw = (float)(yawJoyControlMaxSpeed * pX);
                mThrottle = (float)(verticalJoyControlMaxSpeed * pY);

                if (null == mSendVirtualStickDataTimer) {
                    mSendVirtualStickDataTask = new SendVirtualStickDataTask();
                    mSendVirtualStickDataTimer = new Timer();
                    mSendVirtualStickDataTimer.schedule(mSendVirtualStickDataTask, 0, 200);
                }

            }
        });
    }

    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */
    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (!missingPermission.isEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }

    }

    private void registerApp() {
        SDKManager.getInstance().init(this, new SDKManagerCallback() {

            @Override
            public void onRegisterSuccess() {
                Log.e(TAG, "myApp onRegisterSuccess");
            }

            @Override
            public void onRegisterFailure(IDJIError error) {
                Log.e(TAG, "myApp onRegisterFailure");
            }

            @Override
            public void onProductDisconnect(int productId) {
                Log.e(TAG, "myApp onProductDisconnect");
            }

            @Override
            public void onProductConnect(int productId) {
                Log.e(TAG, "myApp onProductConnect");
            }

            @Override
            public void onProductChanged(int productId) {
                Log.e(TAG, "myApp onProductChanged");
            }

            @Override
            public void onInitProcess(DJISDKInitEvent event, int totalProcess) {
                Log.e(TAG, "myApp onInitProcess");
                if (event == DJISDKInitEvent.INITIALIZE_COMPLETE) {
                    Log.e(TAG, "myApp start registerApp");
                    SDKManager.getInstance().registerApp();
                }
            }

            @Override
            public void onDatabaseDownloadProgress(long current, long total) {
                Log.e(TAG, "myApp onDatabaseDownloadProgress ");
            }
        });
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 定时任务，给无人机发送速度控制指令
    class SendVirtualStickDataTask extends TimerTask {

        @Override
        public void run() {
            if(trackOn) {
                VirtualStickManager.getInstance().setVirtualStickAdvancedModeEnabled(true);
                VirtualStickManager.getInstance().sendVirtualStickAdvancedParam(new VirtualStickFlightControlParam(new Double(mRoll), new Double(mPitch), new Double(mYaw), new Double(mThrottle), VerticalControlMode.VELOCITY, RollPitchControlMode.VELOCITY, YawControlMode.ANGULAR_VELOCITY, FlightCoordinateSystem.BODY));
            } else {
                VirtualStickManager.getInstance().setVirtualStickAdvancedModeEnabled(false);
                VirtualStickManager.getInstance().getLeftStick().setHorizontalPosition((int)mYaw);
                VirtualStickManager.getInstance().getLeftStick().setVerticalPosition((int)mThrottle);
                VirtualStickManager.getInstance().getRightStick().setHorizontalPosition((int)mRoll);
                VirtualStickManager.getInstance().getRightStick().setVerticalPosition((int)mPitch);
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        if (null != mSendVirtualStickDataTimer) {
            mSendVirtualStickDataTask.cancel();
            mSendVirtualStickDataTask = null;
            mSendVirtualStickDataTimer.cancel();
            mSendVirtualStickDataTimer.purge();
            mSendVirtualStickDataTimer = null;
        }
        super.onDestroy();
    }
}

