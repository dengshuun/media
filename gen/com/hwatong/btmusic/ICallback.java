/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\workspace\\MediaUI\\src\\com\\hwatong\\btmusic\\ICallback.aidl
 */
package com.hwatong.btmusic;
public interface ICallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.hwatong.btmusic.ICallback
{
private static final java.lang.String DESCRIPTOR = "com.hwatong.btmusic.ICallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.hwatong.btmusic.ICallback interface,
 * generating a proxy if needed.
 */
public static com.hwatong.btmusic.ICallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.hwatong.btmusic.ICallback))) {
return ((com.hwatong.btmusic.ICallback)iin);
}
return new com.hwatong.btmusic.ICallback.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_onConnected:
{
data.enforceInterface(DESCRIPTOR);
this.onConnected();
return true;
}
case TRANSACTION_onDisconnected:
{
data.enforceInterface(DESCRIPTOR);
this.onDisconnected();
return true;
}
case TRANSACTION_nowPlayingUpdate:
{
data.enforceInterface(DESCRIPTOR);
com.hwatong.btmusic.NowPlaying _arg0;
if ((0!=data.readInt())) {
_arg0 = com.hwatong.btmusic.NowPlaying.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.nowPlayingUpdate(_arg0);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.hwatong.btmusic.ICallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void onConnected() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onConnected, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void onDisconnected() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onDisconnected, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void nowPlayingUpdate(com.hwatong.btmusic.NowPlaying nowPlaying) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((nowPlaying!=null)) {
_data.writeInt(1);
nowPlaying.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_nowPlayingUpdate, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_onConnected = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onDisconnected = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_nowPlayingUpdate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public void onConnected() throws android.os.RemoteException;
public void onDisconnected() throws android.os.RemoteException;
public void nowPlayingUpdate(com.hwatong.btmusic.NowPlaying nowPlaying) throws android.os.RemoteException;
}
