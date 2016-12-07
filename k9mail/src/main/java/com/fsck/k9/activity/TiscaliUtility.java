
package com.fsck.k9.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.fsck.k9.Account;
import com.fsck.k9.K9;
import com.fsck.k9.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

/**
 * Utility class
 * 
 * @author Simona Figus
 *
 */
public class TiscaliUtility {
	public static final String PREFERENCEFILE = "TiscaliPreference";
	/**
	 * Returns the display name for a folder.
	 * 
	 * <p>
	 * This will return localized strings for special IMAP folders.
	 * </p>
	 * 
	 * @param context
	 *         A {@link Context} instance that is used to get the string resources.
	 * @param account
	 *         The {@link Account} the folder belongs to.
	 * @param folderName
	 *         The name of the folder for which to return the display name.
	 * @return The localized IMAP folder name for the provided folder.
	 */
	public static String getDisplayFolderName(Context context, Account account, String folderName) {
		if (account == null)
			return folderName;
		
		final String displayFolderName;
		//INBOX
        if (account.getInboxFolderName() != null && account.getInboxFolderName().equalsIgnoreCase(folderName))
        	displayFolderName = context.getString(R.string.special_mailbox_name_inbox_fmt);
        //Outbox
        else if (account.getOutboxFolderName() != null && account.getOutboxFolderName().equalsIgnoreCase(folderName))
        	displayFolderName = context.getString(R.string.special_mailbox_name_outbox_fmt);
        //Draft
        else if (account.getDraftsFolderName() != null && account.getDraftsFolderName().equalsIgnoreCase(folderName))
        	displayFolderName = context.getString(R.string.special_mailbox_name_drafts_fmt);
        //Trashcan
        else if (account.getTrashFolderName() != null && account.getTrashFolderName().equalsIgnoreCase(folderName))
        	displayFolderName = context.getString(R.string.special_mailbox_name_trash_fmt);
        //Sent
        else if (account.getSentFolderName() != null && account.getSentFolderName().equalsIgnoreCase(folderName))
        	displayFolderName = context.getString(R.string.special_mailbox_name_sent_fmt);
        //Archivio
        else if (account.getArchiveFolderName() != null && account.getArchiveFolderName().equalsIgnoreCase(folderName))
        	displayFolderName = folderName;
        //Spam
        else if (account.getSpamFolderName() != null && account.getSpamFolderName().equalsIgnoreCase(folderName))
        	displayFolderName = context.getString(R.string.special_mailbox_name_spam_fmt);
        else
        	displayFolderName = folderName;
        
        return displayFolderName;
	}
	
	/**
	 * Returns the name for a folder.
	 * 
	 * <p>
	 * This will return names for special IMAP folders.
	 * </p>
	 * 
	 * @param context
	 *         A {@link Context} instance that is used to get the string resources.
	 * @param displayFolderName
	 *         The display name of the folder for which to return the name.
	 * @return The IMAP folder name for the provided folder.
	 */
	public static String getFolderName(Context context, String displayFolderName) {
		if (displayFolderName == null)
			return displayFolderName;
		
		final String folderName;
		//INBOX
		if (displayFolderName.equalsIgnoreCase(context.getString(R.string.special_mailbox_name_inbox_fmt)))
        	folderName = context.getString(R.string.special_mailbox_name_inbox);
        //Outbox
        else if (displayFolderName.equalsIgnoreCase(context.getString(R.string.special_mailbox_name_outbox_fmt)))
        	folderName = context.getString(R.string.special_mailbox_name_outbox);
        //Draft
        else if (displayFolderName.equalsIgnoreCase(context.getString(R.string.special_mailbox_name_drafts_fmt)))
        	folderName = context.getString(R.string.special_mailbox_name_drafts);
        //Trashcan
        else if (displayFolderName.equalsIgnoreCase(context.getString(R.string.special_mailbox_name_trash_fmt)))
        	folderName = context.getString(R.string.special_mailbox_name_trash);
        //Sent
        else if (displayFolderName.equalsIgnoreCase(context.getString(R.string.special_mailbox_name_sent_fmt)))
        	folderName = context.getString(R.string.special_mailbox_name_sent);
        //Archivio
        else if (displayFolderName.equalsIgnoreCase(context.getString(R.string.special_mailbox_name_archive_fmt)))
        	folderName = displayFolderName;
        //Spam
        else if (displayFolderName.equalsIgnoreCase(context.getString(R.string.special_mailbox_name_spam_fmt)))
        	folderName = context.getString(R.string.special_mailbox_name_spam);
        else
        	folderName = displayFolderName;
		
		return folderName;
	}
	
	/**
	 * Checks if the folder is in the top group.
	 *  
	 * @param context
	 *         A {@link Context} instance that is used to get the string resources.
	 * @param folderName
	 *         The name of the folder for which to check if it is in the top group.
	 * @return boolean.
	 */
	public static boolean isFolderInTopGroup(Context context, String folderName) {
		if (folderName == null)
			return false;
		
		//INBOX
        if (folderName.equalsIgnoreCase(context.getString(R.string.special_mailbox_name_inbox))
        //Outbox
        || folderName.equalsIgnoreCase(context.getString(R.string.special_mailbox_name_outbox))
        //Draft
        || folderName.equalsIgnoreCase(context.getString(R.string.special_mailbox_name_drafts))
        //Trashcan
        || folderName.equalsIgnoreCase(context.getString(R.string.special_mailbox_name_trash))
        //Sent
        || folderName.equalsIgnoreCase(context.getString(R.string.special_mailbox_name_sent))
        //Archivio
        //|| folderName.equalsIgnoreCase(context.getString(R.string.special_mailbox_name_archive))
        //Spam
        || folderName.equalsIgnoreCase(context.getString(R.string.special_mailbox_name_spam)))
        	return true;
		
		return false;
	}
	
	/**
     * Sorts the list of folders in the top group.
     *
     * @param context
	 *         A {@link Context} instance that is used to get the string resources.
     * @param list
     * 		   the list of folders to be sorted.
     */
	@SuppressWarnings("unchecked")
	public static <T> void sortFoldersInTopGroup(Context context, List<T> list) {
		/*
		Sort:
		Posta in arrivo
		Bozze
		Posta in uscita
		Inviata
		Spam
		Cestino
		*/
		
		if (!list.isEmpty() && list.get(0) instanceof FolderInfoHolder) {
			HashMap<String, FolderInfoHolder> folders = new HashMap<String, FolderInfoHolder>();
			
			ListIterator<FolderInfoHolder> iterator = (ListIterator<FolderInfoHolder>) list.listIterator();
			while (iterator.hasNext()) {
				FolderInfoHolder folderInfoHolder = iterator.next();
				folders.put(folderInfoHolder.name, folderInfoHolder);
			}
			
			list.clear();
			int i = 0;
			
			//INBOX
			if (folders.containsKey(context.getString(R.string.special_mailbox_name_inbox)))
				((List<FolderInfoHolder>) list).add(i++, folders.get(context.getString(R.string.special_mailbox_name_inbox)));
			//Draft
			if (folders.containsKey(context.getString(R.string.special_mailbox_name_drafts)))
				((List<FolderInfoHolder>) list).add(i++, folders.get(context.getString(R.string.special_mailbox_name_drafts)));
			//Outbox
			if (folders.containsKey(context.getString(R.string.special_mailbox_name_outbox)))
				((List<FolderInfoHolder>) list).add(i++, folders.get(context.getString(R.string.special_mailbox_name_outbox)));
			//Sent
			if (folders.containsKey(context.getString(R.string.special_mailbox_name_sent)))
				((List<FolderInfoHolder>) list).add(i++, folders.get(context.getString(R.string.special_mailbox_name_sent)));
			//Spam
			if (folders.containsKey(context.getString(R.string.special_mailbox_name_spam)))
				((List<FolderInfoHolder>) list).add(i++, folders.get(context.getString(R.string.special_mailbox_name_spam)));
			//Trashcan
			if (folders.containsKey(context.getString(R.string.special_mailbox_name_trash)))
				((List<FolderInfoHolder>) list).add(i++, folders.get(context.getString(R.string.special_mailbox_name_trash)));
		} else	if (!list.isEmpty() && list.get(0) instanceof String) {
			List<String> folders = new ArrayList<String>();
			
			ListIterator<String> iterator = (ListIterator<String>) list.listIterator();
			while (iterator.hasNext())
				folders.add((String) iterator.next());
			
			list.clear();
			int i = 0;
			
			//INBOX
			if (folders.contains(context.getString(R.string.special_mailbox_name_inbox)))
				((List<String>) list).add(i++, context.getString(R.string.special_mailbox_name_inbox));
			//Draft
			if (folders.contains(context.getString(R.string.special_mailbox_name_drafts)))
				((List<String>) list).add(i++, context.getString(R.string.special_mailbox_name_drafts));
			//Outbox
			if (folders.contains(context.getString(R.string.special_mailbox_name_outbox)))
				((List<String>) list).add(i++, context.getString(R.string.special_mailbox_name_outbox));
			//Sent
			if (folders.contains(context.getString(R.string.special_mailbox_name_sent)))
				((List<String>) list).add(i++, context.getString(R.string.special_mailbox_name_sent));
			//Spam
			if (folders.contains(context.getString(R.string.special_mailbox_name_spam)))
				((List<String>) list).add(i++, context.getString(R.string.special_mailbox_name_spam));
			//Trashcan
			if (folders.contains(context.getString(R.string.special_mailbox_name_trash)))
				((List<String>) list).add(i++, context.getString(R.string.special_mailbox_name_trash));
		}
	}
	
	/**
	 * Returns the display error for a message.
	 * 
	 * <p>
	 * This will return localized strings for error messages.
	 * </p>
	 * 
	 * @param errorMessage
	 * 			The message of the error for which to return the display message.
	 * @param values
	 * 			A {@link Context} instance that is used to get the string resources.
	 * @return The localized error message for the provided error.
	 */
	// FIXME verify if useful in Tiscali App
//	public static String getDisplayErrorMessage(String errorMessage, Context... context) {
//		if (errorMessage == null)
//			return errorMessage;
//
//		final String displayErrorMessage;
//		if (errorMessage.toLowerCase(Locale.US).contains("invalid user or password") //Command: AUTHENTICATE PLAIN; response: #2# [NO, invalid user or password]
//				//CRAM-MD5 error: 1 NO [AUTHENTICATIONFAILED] Authentication failed. -> IMAP user e/o password errate
//				|| errorMessage.toLowerCase(Locale.US).contains("authenticationfailed")
//				//Negative SMTP reply: 550 5.1.0 <...@tiscali.it> sender rejected invalid local user -> SMTP locked user
//				|| (errorMessage.toLowerCase(Locale.US).startsWith("negative smtp reply") && errorMessage.toLowerCase(Locale.US).contains("sender rejected invalid local user"))
//				//Negative SMTP reply: 535 5.7.0 ...authentication rejected -> SMTP user errata o locked user
//				|| (errorMessage.toLowerCase(Locale.US).startsWith("negative smtp reply") && errorMessage.toLowerCase(Locale.US).contains("...authentication rejected"))
//				//Negative SMTP reply: 535 5.7.0 No authentication type succeeded -> SMTP password errata
//				|| (errorMessage.toLowerCase(Locale.US).startsWith("negative smtp reply") && errorMessage.toLowerCase(Locale.US).contains("no authentication type succeeded")))
//			displayErrorMessage = context != null && context.length > 0 ? context[0].getString(R.string.error_invalid_username_password_title_tiscali) : K9.app.getString(R.string.error_invalid_username_password_title_tiscali);
//		//WIP (Work In Progress):
//		//Negative SMTP reply: 454 4.7.0 ... authentication failure : try again later -> SMTP mittente in WIP (Work In Progress)
//		else if (errorMessage.toLowerCase(Locale.US).startsWith("negative smtp reply") && errorMessage.toLowerCase(Locale.US).contains("authentication failure") && errorMessage.toLowerCase(Locale.US).contains("try again later"))
//			displayErrorMessage = context != null && context.length > 0 ? context[0].getString(R.string.error_failed_auth_smtp_requested_action_aborted_tiscali) : K9.app.getString(R.string.error_failed_auth_smtp_requested_action_aborted_tiscali);
//		//Frozen:
//		//Negative SMTP reply: 535 5.7.0 sender is frozen
//		else if (errorMessage.toLowerCase(Locale.US).startsWith("negative smtp reply") && errorMessage.toLowerCase(Locale.US).contains("sender is frozen"))
//			displayErrorMessage = context != null && context.length > 0 ? context[0].getString(R.string.error_failed_auth_smtp_frozen_username_title_tiscali) : K9.app.getString(R.string.error_failed_auth_smtp_frozen_username_title_tiscali);
//		//Password too simple:
//		//Negative SMTP reply: 535 5.7.0 authentication rejected: your password is too simple. Errore, password troppo semplice, vedi http://assistenza.tiscali.it/tecnica/posta/authentication_rejected.php
//		else if (errorMessage.toLowerCase(Locale.US).startsWith("negative smtp reply") && errorMessage.toLowerCase(Locale.US).contains("authentication rejected: your password is too simple"))
//			displayErrorMessage = context != null && context.length > 0 ? context[0].getString(R.string.error_failed_auth_smtp_simple_password_title_tiscali) : K9.app.getString(R.string.error_failed_auth_smtp_simple_password_title_tiscali);
//		//BlackList:
//	    //Negative SMTP reply: 550 5.1.0 <testblog6@tiscali.it> sender rejected: Administrative prohibition
//		else if (errorMessage.toLowerCase(Locale.US).startsWith("negative smtp reply") && errorMessage.toLowerCase(Locale.US).contains("sender rejected: administrative prohibition"))
//			displayErrorMessage = context != null && context.length > 0 ? context[0].getString(R.string.error_failed_auth_smtp_black_list_title_tiscali) : K9.app.getString(R.string.error_failed_auth_smtp_black_list_title_tiscali);
//		//Failed to send message:
//		//Negative SMTP reply: 550 5.1.1 <...@tiscali.it> recipient does not exist
//		//Negative SMTP reply: 550 5.1.1 <...@tisca> recipient rejected: domain does not have neither a valid MX or A record
//		//Negative SMTP reply: 550 5.5.0 <...> invalid address
//		else if (errorMessage.toLowerCase(Locale.US).startsWith("negative smtp reply") &&
//					(errorMessage.toLowerCase(Locale.US).contains("recipient does not exist")
//					|| errorMessage.toLowerCase(Locale.US).contains("recipient rejected")
//					|| errorMessage.toLowerCase(Locale.US).contains("invalid address")))
//			displayErrorMessage = context != null && context.length > 0 ? context[0].getString(R.string.error_failed_auth_smtp_recipient_not_exist_tiscali) : K9.app.getString(R.string.error_failed_auth_smtp_recipient_not_exist_tiscali);
//		//Failed to send message
//		//Negative SMTP reply: 550 5.1.0 <...@tiscali.it> sender rejected - il mittente non coincide con la username configurata per l'autenticazione. Vedi http://assistenza.tiscali.it/tecnica/posta/mancato_invio.php
//		else if (errorMessage.toLowerCase(Locale.US).startsWith("negative smtp reply") && errorMessage.toLowerCase(Locale.US).contains("sender rejected - il mittente non coincide con la username"))
//			displayErrorMessage = context != null && context.length > 0 ? context[0].getString(R.string.error_failed_auth_smtp_sender_rejected_tiscali) : K9.app.getString(R.string.error_failed_auth_smtp_sender_rejected_tiscali);
//		//Failed to send message
//		//Negative SMTP reply: 452 4.1.0 <...@tiscali.it> requested action aborted: try again later -> SMTP destinatario in WIP (Work In Progress) o mittente potenziale spammer
//		else if (errorMessage.toLowerCase(Locale.US).startsWith("negative smtp reply") && errorMessage.toLowerCase(Locale.US).contains("requested action aborted: try again later"))
//			displayErrorMessage = context != null && context.length > 0 ? context[0].getString(R.string.error_failed_auth_smtp_requested_action_aborted_tiscali) : K9.app.getString(R.string.error_failed_auth_smtp_requested_action_aborted_tiscali);
//		//Failed to send message:
//		//Message too large for server -> APP error
//		//Negative SMTP reply: 552 5.2.0 ZKpl1q00P1aTBuj01Kqtsq message size is too big -> SMTP error
//		else if (errorMessage.toLowerCase(Locale.US).contains("message too large for server") ||
//				(errorMessage.toLowerCase(Locale.US).startsWith("negative smtp reply") && errorMessage.toLowerCase(Locale.US).contains("message size is too big")))
//			displayErrorMessage = context != null && context.length > 0 ? context[0].getString(R.string.error_failed_auth_smtp_message_too_big_tiscali) : K9.app.getString(R.string.error_failed_auth_smtp_message_too_big_tiscali);
//		//Failed to send message:
//		//Negative SMTP reply: 452 4.1.1 <testblog5@tiscali.it> Too Many Recipients...
//		else if (errorMessage.toLowerCase(Locale.US).startsWith("negative smtp reply") && errorMessage.toLowerCase(Locale.US).contains("too many recipients"))
//			displayErrorMessage = context != null && context.length > 0 ? context[0].getString(R.string.error_failed_auth_smtp_too_many_recipients_tiscali) : K9.app.getString(R.string.error_failed_auth_smtp_too_many_recipients_tiscali);
//		else if (errorMessage.toLowerCase(Locale.US).contains("mailbox does not exist") //Command: SELECT "INBOX"; response: #273# [NO, mailbox does not exist]
//				|| errorMessage.toLowerCase(Locale.US).contains("mailbox doesn't exist")) //Command: SELECT "INBOX"; response: NO Mailbox doesn't exist: INBOX
//			displayErrorMessage = context != null && context.length > 0 ? context[0].getString(R.string.error_folder_not_exist_tiscali) : K9.app.getString(R.string.error_folder_not_exist_tiscali);
//		else if (errorMessage.contains("GaiException") //GaiException->getaddrinfo or getnameinfo methods fail
//				|| errorMessage.contains("UnknownHostException")
//				|| errorMessage.contains("ConnectException")
//				|| errorMessage.contains("SSLException")
//				|| errorMessage.contains("SocketException")//SocketException->No route to host
//				|| errorMessage.toLowerCase(Locale.US).contains("unavailable")//CRAM-MD5 error: 1 NO [UNAVAILABLE] Account is temporarily unavailable.
//				|| errorMessage.toLowerCase(Locale.US).contains("disconnecting") //CRAM-MD5 error: * BYE disconnecting
//				|| errorMessage.toLowerCase(Locale.US).contains("server disconnected")) //Command: AUTHENTICATE CRAM-MD5 - response: #2# [NO, server disconnected]
//			displayErrorMessage = context != null && context.length > 0 ? context[0].getString(R.string.status_network_error) : K9.app.getString(R.string.status_network_error);
//		else if (errorMessage.contains("Exception"))
//				displayErrorMessage = context != null && context.length > 0 ? context[0].getString(R.string.error_generic_tiscali) : K9.app.getString(R.string.error_generic_tiscali);
//		else
//			displayErrorMessage = errorMessage;
//
//		return displayErrorMessage;
//	}
	

	/**
	 * Checks if the message of the error is a temporary error.
	 * 
	 * @param errorMessage
	 * 			The message of the error.
	 * @return boolean.
	 */
	// FIXME verify if useful in Tiscali App
//	public static boolean isTemporaryError(String errorMessage) {
//		if ((errorMessage.toLowerCase(Locale.US).startsWith("negative smtp reply") &&
//				//Negative SMTP reply: 550 5.1.1 <...@tiscali.it> recipient does not exist
//				(errorMessage.toLowerCase(Locale.US).contains("recipient does not exist")
//				//Negative SMTP reply: 550 5.1.1 <...@tisca> recipient rejected: domain does not have neither a valid MX or A record
//				|| errorMessage.toLowerCase(Locale.US).contains("recipient rejected")
//				//Negative SMTP reply: 550 5.5.0 <...> invalid address
//				|| errorMessage.toLowerCase(Locale.US).contains("invalid address")
//				//Negative SMTP reply: 550 5.1.0 <...@tiscali.it> sender rejected - il mittente non coincide con la username configurata per l'autenticazione. Vedi http://assistenza.tiscali.it/tecnica/posta/mancato_invio.php
//				|| errorMessage.toLowerCase(Locale.US).contains("sender rejected - il mittente non coincide con la username")
//				//Negative SMTP reply: 452 4.1.0 <...@tiscali.it> requested action aborted: try again later
//				|| errorMessage.toLowerCase(Locale.US).contains("requested action aborted: try again later")
//				//Negative SMTP reply: 452 4.1.1 <testblog5@tiscali.it> Too Many Recipients...
//				|| errorMessage.toLowerCase(Locale.US).contains("too many recipients")
//				//Negative SMTP reply: 552 5.2.0 ZKpl1q00P1aTBuj01Kqtsq message size is too big -> SMTP error
//				|| errorMessage.toLowerCase(Locale.US).contains("message size is too big")))
//			//Message too large for server -> APP error
//			|| errorMessage.toLowerCase(Locale.US).contains("message too large for server")) {
//
//			return true;
//		}
//
//		return false;
//	}
	
	/**
	 * Returns void.
	 * 
	 * <p>
	 * Displays the sponsored app
	 * </p>
	 * 
	 * @param activity
	 *            Current Activity
	 *            resources.
	 * @return The void.
	 */
	// FIXME verify if useful in Tiscali App
//	public static void viewTopApp(Activity activity) {
//		if (Utility.hasConnectivity(activity.getApplication())) {
//			new TiscaliTopAppSponsored(activity);
//		}
//		return;
//	}
	
	
	/**
	 * Returns void.
	 * 
	 * <p>
	 * open intent of sponsor actionbar tiscali app
	 * </p>
	 * 
	 * @param activity
	 *            Current Activity
	 *  
	 * @return The void.
	 */
	// FIXME verify if useful in Tiscali App
//	public static void linkAppTiscaliGo(final Activity activity) {
//		String packTiscali = activity.getApplicationContext().getString(R.string.package_name_sponsor_action_bar_tiscali);
//		Intent intent = activity.getPackageManager().getLaunchIntentForPackage(packTiscali);
//		if(intent==null){
//			Intent intentMarket = new Intent(Intent.ACTION_VIEW);
//			intentMarket.setData(Uri.parse("market://details?id="
//					+ packTiscali));
//			intentMarket.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_NEW_TASK);
//			activity.startActivity(intentMarket);
//			return;
//		}else{
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_NEW_TASK);
//			activity.startActivity(intent);
//		}
//	}
	
	
	/**
	 * Returns void.
	 * 
	 * <p>
	 * Displays the icon app Tiscali in actionbar in portrait mode
	 * </p>
	 * 
	 * @param activity
	 *            Current Activity
	 *  
	 * @return The void.
	 */
	// FIXME verify if useful in Tiscali App
//	public static void linkAppTiscali(final Activity activity) {
//		String packTiscali = activity.getApplicationContext().getString(R.string.package_name_sponsor_action_bar_tiscali);
//		ImageView im=(ImageView) activity.findViewById(R.id.tiscali_app_adv_ab_top);
//		boolean isInstalled=TiscaliUtility.isAppInstalled(activity.getApplicationContext(), packTiscali);
//
//		if(!isInstalled){
//			if(im !=null){
//				im.setOnClickListener(null);
//				im.setClickable(false);
//				im.setVisibility(View.GONE);
//			}
//			return;
//		}
//
//		if(im != null){ // caso immagine in alto
//			im.setVisibility(View.VISIBLE);
//			im.setClickable(true);
//			im.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					TiscaliUtility.linkAppTiscaliGo(activity);
//				}
//			});
//		}
//		return;
//	}

	/**
	 * Returns the suffix device type for the current activity empty is
	 * smartphone
	 * 
	 * <p>
	 * Get The sufix for current activity (from density dpi)
	 * </p>
	 * 
	 * @param Activity
	 *            Current Activity
	 * @return String with the suffix or empty string for smartphone.
	 */
	// FIXME verify if useful in Tiscali App
//	public static String getSufixByActivity(Activity activity) {
//		if (TiscaliUtility.isTabletDevice(activity.getResources()))
//			return "-tablet";
//
//		return "";
//	}
	
	/**
	 * Checks if the device is a tablet.
	 * 
	 * @param resources
	 * 			A {@link Resources} instance that is used to get the screen's configuration.
	 * @return boolean.
	 */
	// FIXME verify if useful in Tiscali App
//	public static boolean isTabletDevice(Resources resources) {
//	    int screenLayout = resources.getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
//	    boolean isScreenLarge = (screenLayout == Configuration.SCREENLAYOUT_SIZE_LARGE);
//	    boolean isScreenXlarge = (screenLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE);
//
//	    return (isScreenLarge || isScreenXlarge);
//	}

	/**
	 * Returns boolean check if the app is installed
	 * 
	 * <p>
	 * Displays the sponsored app
	 * </p>
	 * 
	 * @param context
	 *            A {@link Context} instance that is used to get the string
	 *            resources.
	 * @param String
	 *            uri app
	 * @return boolean app installed.
	 */
	// FIXME verify if useful in Tiscali App
//	public static boolean isAppInstalled(Context context, String uri) {
//		PackageManager pm = context.getPackageManager();
//		boolean app_installed = false;
//		try {
//			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
//			app_installed = true;
//		} catch (PackageManager.NameNotFoundException e) {
//			app_installed = false;
//		}
//		return app_installed;
//	}
	
	/**
	 * Prints the message
	 * 
	 * @param activity
	 * 			A {@link Activity} instance that is used to start an Intent and to get the string resources
	 * @param message
	 * 			The message to print
	 */
	// FIXME verify if useful in Tiscali App
//	public static void openPrint(final Activity activity, Message message) {
//		if (message != null)
//			TiscaliPrintActivity.printMessage(activity, message);
//	}
	
	/**
	 * Opens the help wizard
	 *
	 * <p>
	 * This will call the help wizard.
	 * </p>
	 *
	 * @param activity
	 * 			A {@link Activity} instance that is used to start an Intent or to show an AlertDialog and to get the string resources
	 * @return AlertDialog
	 * 			An AlertDialog returned if help wizard not showable
	 */
	// FIXME verify if useful in Tiscali App
//	public static AlertDialog openHelp(final Activity activity) {
//		if (!TiscaliInfoWizardActivity.showInfoWizard(activity, true)) {
//			final WebView wView = new WebView(activity);
//    		wView.setWebViewClient(K9WebViewClient.newInstance());
//    		wView.loadData(activity.getString(R.string.info_wizard_message_tiscali), "text/html", "utf-8");
//
//    		AlertDialog helpAlertDialog = new AlertDialog.Builder(activity)
//    		.setTitle(R.string.info_wizard_title_tiscali)
//            .setView(wView)
//            .setCancelable(true)
//            .setPositiveButton(R.string.okay_action, new DialogInterface.OnClickListener() {
//            	@Override
//                public void onClick(DialogInterface dialog, int which) {
//            		dialog.dismiss();
//                }
//            })
//            .create();
//    		helpAlertDialog.show();
//
//    		return helpAlertDialog;
//    	}
//
//		return null;
//	}
	
	/**
	 * Opens the guide link
	 *
	 * <p>
	 * This will create an Intent to the guide.
	 * </p>
	 *
	 * @param activity
	 * 			A {@link Activity} instance that is used to start an Intent
	 */
	// FIXME verify if useful in Tiscali App
//	public static void linkGuide(final Activity activity) {
//		Intent guideIntent = new Intent(Intent.ACTION_VIEW);
//		guideIntent.setData(Uri.parse(activity.getString(R.string.guide_action_url)));
//		guideIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_NEW_TASK);
//		activity.startActivity(guideIntent);
//	}
	
	/**
	 * Returns the date resulting from the parsing.
	 * 
	 * <p>
	 * This will return the date resulting from the parsing.
	 * </p>
	 * 
	 * @param malformedDateString
	 * 			The malformed date string to parse
	 * @return Date
	 * 			The date resulting from the parsing
	 */
	// FIXME verify if useful in Tiscali App
//	public static Date malformedDateStringToDate(String malformedDateString) {
//		Date date = null;
//
//		if (TextUtils.isEmpty(malformedDateString))
//			return date;
//
//		SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss ZZZ", Locale.US);
//		try {
//			date = sdf.parse(malformedDateString);
//		} catch (ParseException e) {
//			Log.e(K9.LOG_TAG, "ParseException in TiscaliUtility - malformedDateStringToDate: " + e);
//		}
//
//		return date;
//	}
	
	/**
	 * Sets the notification background color from Android Marshmallow and later.
	 * 
	 * <p>
	 * This will set the notification background color from Android Marshmallow and later.
	 * </p>
	 * 
	 * @param builder
	 * 			A {@link NotificationCompat.Builder} instance that is used to set the notification background color
	 */
	// FIXME verify if useful in Tiscali App
//	public static void setNotificationBackgroundColor(final NotificationCompat.Builder builder) {
//		if (Build.VERSION.SDK_INT >= 23)//Android Marshmallow 6.0 Api Level 23
//        	builder.setColor(builder.mContext.getResources().getColor(R.color.tiscali_background_action_bar));
//	}
}