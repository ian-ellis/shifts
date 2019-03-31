package android.arch.lifecycle

fun ViewModel.callClear(){
    this.onCleared()
}