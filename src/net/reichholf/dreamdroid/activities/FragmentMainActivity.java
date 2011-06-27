/* © 2010 Stephan Reichholf <stephan at reichholf dot net>
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package net.reichholf.dreamdroid.activities;

import net.reichholf.dreamdroid.DreamDroid;
import net.reichholf.dreamdroid.R;
import net.reichholf.dreamdroid.abstivities.MultiPaneHandler;
import net.reichholf.dreamdroid.fragment.ActivityCallbackHandler;
import net.reichholf.dreamdroid.fragment.NavigationFragment;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.TextView;

/**
 * @author sre
 *
 */
public class FragmentMainActivity extends FragmentActivity implements MultiPaneHandler {	
	private boolean mMultiPane;
	
	private Fragment mCurrentDetailFragment;	
	private FragmentManager mFragmentManager;
	private NavigationFragment mNavigationFragment;
	private ActivityCallbackHandler mCallBackHandler;
	
	
	private boolean isNavigationDialog = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState){		
		super.onCreate(savedInstanceState);	
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		checkLayout();
		
		mFragmentManager = getSupportFragmentManager();				
		
		if(savedInstanceState != null){
			mNavigationFragment = (NavigationFragment) mFragmentManager.getFragment(savedInstanceState, "navigation");
		}
		if(mNavigationFragment == null){
			mNavigationFragment = new NavigationFragment(); 
		}
				
		if(savedInstanceState != null){
			mCurrentDetailFragment = mFragmentManager.getFragment(savedInstanceState, "current");
		}
				
		initViews();
		mNavigationFragment.setHighlightCurrent(mMultiPane);
	}
	
	private void initViews(){
		setContentView(R.layout.dualpane);
		
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		showFragment(ft, R.id.navigation_view, mNavigationFragment);
		if(mCurrentDetailFragment != null){
			showFragment(ft, R.id.detail_view, mCurrentDetailFragment);
			mCallBackHandler = (ActivityCallbackHandler) mCurrentDetailFragment;
		}
		ft.commit();
	}
	
	private void showFragment(FragmentTransaction ft, int viewId, Fragment fragment){
		if( fragment.isAdded() ){
			Log.i(DreamDroid.LOG_TAG, "Fragment already added, showing");
			ft.show(fragment);
		} else {
			Log.i(DreamDroid.LOG_TAG, "Fragment not added, adding");
			ft.replace(viewId, fragment, fragment.getClass().getSimpleName());	
		}		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.reichholf.dreamdroid.abstivities.AbstractHttpListActivity#
	 * onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		mFragmentManager.putFragment(outState, "navigation", mNavigationFragment);
		if(mCurrentDetailFragment != null){
			mFragmentManager.putFragment(outState, "current", mCurrentDetailFragment);
		}		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onResume(){
		super.onResume();
	}
	
	
	/**
	 * Set mMultiPane to true if screen size is at least SCREENLAYOUT_SIZE_XLARGE, else false
	 */
	private void checkLayout(){		
		final int SCREENLAYOUT_SIZE_XLARGE = 4; // Not available until API 9.
        mMultiPane = (getResources().getConfiguration().screenLayout & SCREENLAYOUT_SIZE_XLARGE) != 0;
	}
	
	/**
	 * @param fragmentClass
	 */
	@SuppressWarnings("rawtypes")
	public void showDetails(Class fragmentClass){
		if(mMultiPane){
			try {
				Fragment fragment = (Fragment) fragmentClass.newInstance();
				showDetails(fragment);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Intent intent = new Intent(this, SimpleFragmentActivity.class);
			intent.putExtra("fragmentClass", fragmentClass);
			startActivity(intent);
		}
			
	}
	
	@Override
	public void showDetails(Fragment fragment){
		showDetails(fragment, false);
	}
	
	public void back(){
		mFragmentManager.popBackStackImmediate();
	}
	
	/**
	 * @param fragment
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@Override
	public void showDetails(Fragment fragment, boolean addToBackStack){	
		mCallBackHandler = (ActivityCallbackHandler) fragment;
		if(mMultiPane){			
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			mCurrentDetailFragment = fragment;
			showFragment(ft, R.id.detail_view, fragment);
//			ft.replace(R.id.detail_view, fragment, mCurrentDetailFragment.getClass().getSimpleName());
			if(addToBackStack){
				ft.addToBackStack(null);
			}
			ft.commit();
		} else { //TODO Error Handling
			Intent intent = new Intent(this, SimpleFragmentActivity.class);
			intent.putExtra("fragmentClass", fragment.getClass());
			intent.putExtras(fragment.getArguments());
			startActivity(intent);
		}
	}
	
	@Override
	public void setTitle(CharSequence title){
		if(mMultiPane){
			TextView t = (TextView) findViewById(R.id.detail_title);
			t.bringToFront();
			String replaceMe = getText(R.string.app_name) + "::";
			t.setText(title.toString().replace(replaceMe, ""));
			return;
		}
		super.setTitle(title);
	}
	
	public void setIsNavgationDialog(boolean is){
		isNavigationDialog = is;
	}
	
	@Override
	protected Dialog onCreateDialog(int id){
		Dialog dialog = null;
		if(isNavigationDialog){
			dialog = mNavigationFragment.onCreateDialog(id);
			isNavigationDialog = false;
		} else {
			dialog = mCallBackHandler.onCreateDialog(id);
		}
		
		if(dialog == null){
			dialog = super.onCreateDialog(id);
		}
		
		return dialog;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if(mCallBackHandler != null){
			if(mCallBackHandler.onKeyDown(keyCode, event)){
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event){
		if(mCallBackHandler != null){
			if(mCallBackHandler.onKeyUp(keyCode, event)){
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}
	
	public boolean isMultiPane(){
		return mMultiPane;
	}
	
	@Override
	public void finish(){
		if(mMultiPane){
			mFragmentManager.popBackStackImmediate();
		} else {
			super.finish();
		}
	}
}