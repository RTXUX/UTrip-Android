package xyz.rtxux.utrip.android.ui.profileedit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileEditViewModel : ViewModel() {
    val editable = MutableLiveData<Boolean>(false)
}
