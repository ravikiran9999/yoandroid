/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class VideoSwitchParam {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected VideoSwitchParam(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(VideoSwitchParam obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        pjsua2JNI.delete_VideoSwitchParam(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setTarget_id(int value) {
    pjsua2JNI.VideoSwitchParam_target_id_set(swigCPtr, this, value);
  }

  public int getTarget_id() {
    return pjsua2JNI.VideoSwitchParam_target_id_get(swigCPtr, this);
  }

  public VideoSwitchParam() {
    this(pjsua2JNI.new_VideoSwitchParam(), true);
  }

}
