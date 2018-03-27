/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.8
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public final class pjsip_event_id_e {
  public final static pjsip_event_id_e PJSIP_EVENT_UNKNOWN = new pjsip_event_id_e("PJSIP_EVENT_UNKNOWN");
  public final static pjsip_event_id_e PJSIP_EVENT_TIMER = new pjsip_event_id_e("PJSIP_EVENT_TIMER");
  public final static pjsip_event_id_e PJSIP_EVENT_TX_MSG = new pjsip_event_id_e("PJSIP_EVENT_TX_MSG");
  public final static pjsip_event_id_e PJSIP_EVENT_RX_MSG = new pjsip_event_id_e("PJSIP_EVENT_RX_MSG");
  public final static pjsip_event_id_e PJSIP_EVENT_TRANSPORT_ERROR = new pjsip_event_id_e("PJSIP_EVENT_TRANSPORT_ERROR");
  public final static pjsip_event_id_e PJSIP_EVENT_TSX_STATE = new pjsip_event_id_e("PJSIP_EVENT_TSX_STATE");
  public final static pjsip_event_id_e PJSIP_EVENT_USER = new pjsip_event_id_e("PJSIP_EVENT_USER");

  public final int swigValue() {
    return swigValue;
  }

  public String toString() {
    return swigName;
  }

  public static pjsip_event_id_e swigToEnum(int swigValue) {
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (int i = 0; i < swigValues.length; i++)
      if (swigValues[i].swigValue == swigValue)
        return swigValues[i];
    throw new IllegalArgumentException("No enum " + pjsip_event_id_e.class + " with value " + swigValue);
  }

  private pjsip_event_id_e(String swigName) {
    this.swigName = swigName;
    this.swigValue = swigNext++;
  }

  private pjsip_event_id_e(String swigName, int swigValue) {
    this.swigName = swigName;
    this.swigValue = swigValue;
    swigNext = swigValue+1;
  }

  private pjsip_event_id_e(String swigName, pjsip_event_id_e swigEnum) {
    this.swigName = swigName;
    this.swigValue = swigEnum.swigValue;
    swigNext = this.swigValue+1;
  }

  private static pjsip_event_id_e[] swigValues = { PJSIP_EVENT_UNKNOWN, PJSIP_EVENT_TIMER, PJSIP_EVENT_TX_MSG, PJSIP_EVENT_RX_MSG, PJSIP_EVENT_TRANSPORT_ERROR, PJSIP_EVENT_TSX_STATE, PJSIP_EVENT_USER };
  private static int swigNext = 0;
  private final int swigValue;
  private final String swigName;
}

