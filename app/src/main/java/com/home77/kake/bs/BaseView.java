package com.home77.kake.bs;

import com.home77.common.base.collection.Params;

/**
 * @author CJ
 */
public interface BaseView {
  void onCommand(CmdType cmdType, Params in, Params out);
}