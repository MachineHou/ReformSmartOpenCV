package tech.huqi.smartopencvdemo.opencv;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.samples.facedetect.DetectionBasedTracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import tech.huqi.smartopencv.SmartOpenCV;
import tech.huqi.smartopencv.core.preview.CameraConfiguration;
import tech.huqi.smartopencvdemo.R;
import tech.huqi.smartopencvdemo.db.DatabaseHelper;
import tech.huqi.smartopencvdemo.db.UserInfo;

public class FdActivity extends CameraActivity implements CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    public static final int JAVA_DETECTOR = 0;
    public static final int NATIVE_DETECTOR = 1;

    private MenuItem mItemFace50;
    private MenuItem mItemFace40;
    private MenuItem mItemFace30;
    private MenuItem mItemFace20;
    private MenuItem mItemType;

    private Mat mRgba;
    private Mat mGray;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;
    private DetectionBasedTracker mNativeDetector;

    private int mDetectorType = NATIVE_DETECTOR;
    private String[] mDetectorName;

    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;
    public final static int FLAG_REGISTER = 1;
    public final static int FLAG_VERIFY = 2;

    private Bitmap mDetectedFace;
    List<UserInfo> userList;
    private FaceMatcher1 matcher1;
    private FaceMatcher2 matcher2;

    private CameraBridgeViewBase mOpenCvCameraView;
//    private JavaCameraView mOpenCvCameraView;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Intent intent;
            switch (msg.what) {
                case FLAG_REGISTER:
                    if (mDetectedFace == null) {
                        mDetectedFace = (Bitmap) msg.obj;
                        int result = matcher1.histogramMatch(mDetectedFace);
                        Log.e(TAG, mDetectedFace + "mHandler: " + result);
                        if (result == matcher1.UNFINISHED) {
                            mDetectedFace = null;
                        } else if (result == matcher1.NO_MATCHER) {
                            intent = new Intent(FdActivity.this,
                                    RegisterActivity.class);
                            intent.putExtra("Face", mDetectedFace);
                            startActivity(intent);
                            finish();
                        } else {
                            intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                    break;
                case FLAG_VERIFY:
                    if (mDetectedFace == null) {
                        mDetectedFace = (Bitmap) msg.obj;
                        int result = matcher2.histogramMatch(FdActivity.this, mDetectedFace);
                        if (result == matcher2.UNFINISHED) {
                            mDetectedFace = null;
                        }
//                        else if (result == matcher2.NO_MATCHER) {
//                            intent = new Intent();
//                            setResult(RESULT_CANCELED, intent);
//                            finish();
//                            ToastUtil.showToast(FdActivity.this, "?????????????????????????????????", 1);
//                        }
                        else {
                            intent = new Intent();
                            intent.putExtra("USER_ID", result);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else {
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
                        }

                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public FdActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detect_surface_view);

//        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView = findViewById(R.id.fd_activity_surface_view);

        SmartOpenCV.getInstance().init(mOpenCvCameraView, new CameraConfiguration.Builder()
                .debug(true)
                .cameraIndex(1)      // ?????????????????????,????????????????????????????????????????????????frontCamera
                .keepScreenOn(false) // ????????????????????????
                .frontCamera(true)   // ???????????????????????????
                .openCvDefaultDrawStrategy(false)      // ????????????OpenCV?????????????????????????????????
                .openCvDefaultPreviewCalculator(false) // ????????????OpenCV????????????????????????????????????
                .landscape(false)     // ??????????????????
                .enableFpsMeter(false) // ???????????????????????????
                .usbCamera(false)     // ????????????USB?????????????????????????????????USB???????????????????????????true
                .bitmapConfig(Bitmap.Config.RGB_565) // ???????????????????????????
                .maxFrameSize(400, 320)     // ??????????????????????????????
                .cvCameraViewListener(this) // ??????OpenCV???????????????
//                .previewSizeCalculator(new IPreviewSizeCalculator() { // ????????????????????????????????????
//                    @Override
//                    public Size calculateCameraFrameSize(List<Size> supportedSizes, int surfaceWidth, int surfaceHeight) {
//                        // ????????????????????????????????????????????????????????????????????????????????????
//                        return new Size(1080, 1920);
//                    }
//                })
//                .drawStrategy(new IDrawStrategy() { // ?????????????????????
//                    @Override
//                    public void drawBitmap(Canvas canvas, Bitmap frameBitmap, int surfaceWidth, int surfaceHeight) {
//                        // ????????????????????????????????????????????????????????????????????????????????????
//
//                    }
//                })
                .build());

        DatabaseHelper helper = new DatabaseHelper(FdActivity.this);
        userList = helper.query();
        matcher1 = new FaceMatcher1(userList);
        matcher2 = new FaceMatcher2(userList);
        helper.close();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();
        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null) {
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
            }
        } else if (mDetectorType == NATIVE_DETECTOR) {
            if (mNativeDetector != null) {
                mNativeDetector.detect(mGray, faces);
            }
        } else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {
            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
//            Log.e(TAG, "onCameraFrame1: " + facesArray[i]);
//            Point point = new Point(facesArray[i].x + 320, facesArray[i].y + 320);
//            facesArray[i] = new Rect(point, facesArray[i].size());

//            if (facesArray[i].height > 480 && facesArray[i].height < 640) {
            if (facesArray[i].height > 200 && facesArray[i].height < 1024) {
                Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(),
                        FACE_RECT_COLOR, 3);
                Log.e(TAG, "onCameraFrame3: " + facesArray[i].height);
                // ???????????????message???????????????????????????
                Mat faceMat = new Mat(mRgba, facesArray[i]);
                Imgproc.resize(faceMat, faceMat, new Size(320, 320));
                Bitmap bitmap = Bitmap.createBitmap(faceMat.width(),
                        faceMat.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(faceMat, bitmap);
                Message message = Message.obtain();
                message.what = getIntent().getIntExtra("flag", 0);
                message.obj = bitmap;
                mHandler.sendMessage(message);
            }

        }

        return mRgba;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
        mItemType = menu.add(mDetectorName[mDetectorType]);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemFace50) {
            setMinFaceSize(0.5f);
        } else if (item == mItemFace40) {
            setMinFaceSize(0.4f);
        } else if (item == mItemFace30) {
            setMinFaceSize(0.3f);
        } else if (item == mItemFace20) {
            setMinFaceSize(0.2f);
        } else if (item == mItemType) {
            int tmpDetectorType = (mDetectorType + 1) % mDetectorName.length;
            item.setTitle(mDetectorName[tmpDetectorType]);
            setDetectorType(tmpDetectorType);
        }
        return true;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    private void setDetectorType(int type) {
        if (mDetectorType != type) {
            mDetectorType = type;

            if (type == NATIVE_DETECTOR) {
                Log.i(TAG, "Detection Based Tracker enabled");
                mNativeDetector.start();
            } else {
                Log.i(TAG, "Cascade detector enabled");
                mNativeDetector.stop();
            }
        }
    }
}


