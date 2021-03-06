/*******************************************************************************
 * Copyright 2013 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.	
 *******************************************************************************/
package uk.co.senab.photup.tasks;

import java.lang.ref.WeakReference;
import java.util.List;

import org.json.JSONException;

import uk.co.senab.photup.facebook.FacebookRequester;
import uk.co.senab.photup.listeners.FacebookErrorListener;
import uk.co.senab.photup.model.Account;
import uk.co.senab.photup.model.FbUser;
import android.content.Context;
import android.os.AsyncTask;

import com.facebook.android.FacebookError;

public class FriendsAsyncTask extends AsyncTask<Void, Void, List<FbUser>> {

	public static interface FriendsResultListener extends FacebookErrorListener {
		public void onFriendsLoaded(List<FbUser> friends);
	}

	private final WeakReference<Context> mContext;
	private final WeakReference<FriendsResultListener> mListener;

	public FriendsAsyncTask(Context context, FriendsResultListener listener) {
		mContext = new WeakReference<Context>(context);
		mListener = new WeakReference<FriendsResultListener>(listener);
	}

	@Override
	protected List<FbUser> doInBackground(Void... params) {
		Context context = mContext.get();
		if (null != context) {
			Account account = Account.getAccountFromSession(context);
			if (null != account) {
				try {
					FacebookRequester requester = new FacebookRequester(account);
					return requester.getFriends();
				} catch (FacebookError e) {
					FriendsResultListener listener = mListener.get();
					if (null != listener) {
						listener.onFacebookError(e);
					} else {
						e.printStackTrace();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(List<FbUser> result) {
		super.onPostExecute(result);

		FriendsResultListener listener = mListener.get();
		if (null != result && null != listener) {
			listener.onFriendsLoaded(result);
		}
	}

}
