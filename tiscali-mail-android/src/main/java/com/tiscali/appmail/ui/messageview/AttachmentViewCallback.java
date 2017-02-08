package com.tiscali.appmail.ui.messageview;


import com.tiscali.appmail.mailstore.AttachmentViewInfo;


interface AttachmentViewCallback {
    void onViewAttachment(AttachmentViewInfo attachment);

    void onSaveAttachment(AttachmentViewInfo attachment);

    void onSaveAttachmentToUserProvidedDirectory(AttachmentViewInfo attachment);
}
