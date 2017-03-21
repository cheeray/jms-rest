package com.cheeray.jms.annotation;

public enum BackoutMode {
	EACH_TRIED_ALL_FAILED,
	EACH_TRIED_ANY_FAILURE,
	ANY_FAILURE_STOP_THEN_BACKOUT;

}
