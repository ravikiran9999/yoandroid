/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class OnTimerParam {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected OnTimerParam(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(OnTimerParam obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        pjsua2JNI.delete_OnTimerParam(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setUserData(SWIGTYPE_p_void value) {
    pjsua2JNI.OnTimerParam_userData_set(swigCPtr, this, SWIGTYPE_p_void.getCPtr(value));
  }

  public SWIGTYPE_p_void getUserData() {
    long cPtr = pjsua2JNI.OnTimerParam_userData_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public void setMsecDelay(long value) {
    pjsua2JNI.OnTimerParam_msecDelay_set(swigCPtr, this, value);
  }

  public long getMsecDelay() {
    return pjsua2JNI.OnTimerParam_msecDelay_get(swigCPtr, this);
  }

  public OnTimerParam() {
    this(pjsua2JNI.new_OnTimerParam(), true);
  }

}
