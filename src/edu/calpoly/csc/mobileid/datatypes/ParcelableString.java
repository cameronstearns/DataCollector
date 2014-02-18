package edu.calpoly.csc.mobileid.datatypes;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableString implements Parcelable {
   private String string;

   public ParcelableString(String string) {
      this.string = string;
   }
   public ParcelableString(Parcel in) {
      this.string = in.readString();
   }

   @Override
   public int describeContents() {
      return 0;
   }

   @Override
   public void writeToParcel(Parcel dest, int flags) {
      dest.writeString(string);
   }
   
   public static final Parcelable.Creator<ParcelableString> CREATOR = new Parcelable.Creator<ParcelableString>() {
      public ParcelableString createFromParcel(Parcel in) {
         return new ParcelableString(in);
      }

      public ParcelableString[] newArray(int size) {
         return new ParcelableString[size];
      }
   };
}
