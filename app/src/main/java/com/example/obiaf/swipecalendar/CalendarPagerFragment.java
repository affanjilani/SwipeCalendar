package com.example.obiaf.swipecalendar;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Created by obiaf on 5/30/2018.
 */

public class CalendarPagerFragment extends Fragment {
    private RecyclerView customCalendar;

    //Overview Dialog
    private boolean isLongPressed = false;
    private Dialog dayOverviewDialog;
    private TextView dialogDate;
    private RecyclerView dialogList;

    //Calendar Grid
    private Calendar monthToShow = new GregorianCalendar();
    private Calendar today = new GregorianCalendar();
    private int numDaysSelected;
    private Calendar[] selectedDays = new Calendar[7];
    private int posSelected;
    private CustomCalendarView.SelectionObserver selectionObserver;
    private ArrayList<String> daysOfMonth;
    private int offset=0;

    //Calendar attributes
    boolean multiDaySelect;
    Typeface typeface;

    //List of user sessions, preferably for the month being shown
    ArrayList<Session> sessions;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.calendar_grid, container, false);

        customCalendar = (RecyclerView) rootView.findViewById(R.id.customCalendar);
        customCalendar.hasFixedSize();

        //Removes date selection animation
        ((SimpleItemAnimator) customCalendar.getItemAnimator()).setSupportsChangeAnimations(false);

        //find the correct month to show using the offset
        Bundle bundle = this.getArguments();
        if(bundle!=null) this.offset = bundle.getInt("offset", 0);

        this.monthToShow.add(Calendar.MONTH, offset);

        this.daysOfMonth  = makeDaySet(offset);

        setAdapter();

        return rootView;

    }

    public void setAdapter(){

        //set adapter
        if(daysOfMonth!=null){
            CalendarAdapter adapter = new CalendarAdapter(getContext(), daysOfMonth);
            customCalendar.setAdapter(adapter);

            customCalendar.setLayoutManager(new GridLayoutManager(getContext(), 7));
        }

    }

    //Adapter for the grid recyclerView
    private class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

        //create variable bindings for any data adapter will need
        ArrayList<String> daysOfMonth;
        Context context;


        //constructor
        public CalendarAdapter(Context context, ArrayList<String> daysOfMonth){
            this.context = context;
            this.daysOfMonth = daysOfMonth;
        }


        //Holds all of the views that will be part of the grid
        public class ViewHolder extends RecyclerView.ViewHolder{
            //Grid cell elements
            RelativeLayout outsideGridCell;
            LinearLayout insideGridCell;
            TextView currentDateCircle;
            TextView dayText;
            ImageView eventSticker;


            //TODO: set onclicklisteners here
            public ViewHolder(View itemView){
                super(itemView);

                //bind all elements to their respective views
                outsideGridCell = itemView.findViewById(R.id.outsideGridCell);
                insideGridCell = itemView.findViewById(R.id.insideGridCell);
                currentDateCircle = itemView.findViewById(R.id.currentDateCircle);
                dayText = itemView.findViewById(R.id.dayText);
                eventSticker = itemView.findViewById(R.id.eventSticker);

                //Dialog used to display events on a certain day or friends with events that day (later)
                dayOverviewDialog = initOverviewDialog();


                //Click listener to select day
                outsideGridCell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = getAdapterPosition();

                        String[] selectedDayInfo = daysOfMonth.get(pos).split("/");
                        Calendar selectedDay = new GregorianCalendar(Integer.parseInt(selectedDayInfo[2]),
                                Integer.parseInt(selectedDayInfo[1]), Integer.parseInt(selectedDayInfo[0]));

                        //if we are in the multi select calendar
                        if(multiDaySelect) {
                            //if the day selected is in the same week as the other days selected
                            if (numDaysSelected > 0 && numDaysSelected <= 7) {
                                Calendar reference = null;
                                for (int i = 0; i < selectedDays.length; i++) {
                                    if (selectedDays[i] != null) {
                                        reference = selectedDays[i];
                                        break;
                                    }
                                }

                                if (reference == null) return;

                                if (selectedDay.get(Calendar.WEEK_OF_YEAR) == reference.get(Calendar.WEEK_OF_YEAR)) {

                                    if (isSameDay(selectedDay, selectedDays[selectedDay.get(Calendar.DAY_OF_WEEK) - 1])) {
                                        selectedDays[selectedDay.get(Calendar.DAY_OF_WEEK) - 1] = null;
                                        if(selectionObserver!=null) selectionObserver.update(selectedDays, posSelected);

                                        numDaysSelected--;

                                        notifyItemChanged(pos);
                                    } else {
                                        selectedDays[selectedDay.get(Calendar.DAY_OF_WEEK) - 1] = selectedDay;
                                        if(selectionObserver!=null) selectionObserver.update(selectedDays, posSelected);

                                        numDaysSelected++;

                                        notifyItemChanged(pos);
                                    }

                                }
                                //if not then it is another week so we reinit the array, and remove old selections
                                else {
                                    Calendar oldDate = null;
                                    for (int oldPos = 0; oldPos < 7; oldPos++) {
                                        if (selectedDays[oldPos] != null)
                                            oldDate = selectedDays[oldPos];
                                    }
                                    if (oldDate == null) return;

                                    else {
                                        reinitSelections();                 //reinitialize array

                                        int changeRangeStart = (oldDate.get(Calendar.WEEK_OF_MONTH) - 1) * 7;

                                        //start on the sunday and deselect whole week
                                        notifyItemRangeChanged(changeRangeStart, 7);

                                        selectedDays[selectedDay.get(Calendar.DAY_OF_WEEK) - 1] = selectedDay;
                                        if(selectionObserver!=null) selectionObserver.update(selectedDays, posSelected);
                                        numDaysSelected = 1;

                                        notifyItemChanged(pos);
                                    }
                                }
                            } else if (numDaysSelected == 0) {
                                selectedDays[selectedDay.get(Calendar.DAY_OF_WEEK) - 1] = selectedDay;
                                if(selectionObserver!=null) selectionObserver.update(selectedDays, posSelected);

                                notifyItemChanged(pos);

                                numDaysSelected = 1;
                            }
                        }

                        //Single day select
                        else {

                            //notify that we must unselect previous selection
                            notifyItemChanged(posSelected);

                            posSelected = pos;

                            selectedDays[0] = selectedDay;

                            if(selectionObserver!=null) selectionObserver.update(selectedDays, posSelected);

                            //change to new selection
                            notifyItemChanged(pos);
                        }

                    }
                });

                //Long press listener to open up overview dialog
                outsideGridCell.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {

                        //get the day being long pressed
                        int pos = getAdapterPosition();
                        String[] longPressedDayInfo = daysOfMonth.get(pos).split("/");
                        Calendar longPressedDay = new GregorianCalendar(Integer.parseInt(longPressedDayInfo[2]),
                                Integer.parseInt(longPressedDayInfo[1]), Integer.parseInt(longPressedDayInfo[0]));


                        refreshOverviewDialog(longPressedDay);

                        dayOverviewDialog.show();

                        isLongPressed = true;

                        //Listener to close overview dialog once long press stops
                        outsideGridCell.setOnTouchListener(dismissOverviewListener(dayOverviewDialog));

                        //if user moves finger while still longpressing, ACTION_UP detected by the view, not dialog
                        customCalendar.setOnTouchListener(dismissOverviewListener(dayOverviewDialog));

                        return true;
                    }
                });
            }

            //Method to generate the dismiss listener for day overview dialogs.
            private View.OnTouchListener dismissOverviewListener(final Dialog overviewDialog){
                return new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        view.onTouchEvent(motionEvent);

                        if(isLongPressed) {
                            //Make sure the recyclerView does not scroll in the background
                            view.getParent().requestDisallowInterceptTouchEvent(true);

                            //if the finger is going up
                            if(motionEvent.getAction() == MotionEvent.ACTION_UP ||
                                    motionEvent.getAction() == MotionEvent.ACTION_CANCEL){
                                //Allow parent views to intercept touch events again
                                view.getParent().requestDisallowInterceptTouchEvent(false);

                                overviewDialog.dismiss();

                                isLongPressed = false;
                            }
                        }

                        return false;
                    }
                };
            }
        }

        @Override
        public CalendarAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            LayoutInflater inflater = LayoutInflater.from(this.context);
            View calendar = inflater.inflate(R.layout.calendar_day_cell,parent,false);

            ViewHolder viewHolder = new ViewHolder(calendar);
            viewHolder.dayText.setTypeface(typeface);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(CalendarAdapter.ViewHolder viewHolder, final int position){
            //Calendar day = this.daysOfMonth.get(position);      //gets day at the position we want to fill
            String dayAtPosition = this.daysOfMonth.get(position);
            String[] dmy = dayAtPosition.split("/");
            Calendar day = new GregorianCalendar(Integer.parseInt(dmy[2]),Integer.parseInt(dmy[1]),Integer.parseInt(dmy[0]));

            viewHolder.dayText.setText(""+day.get(Calendar.DAY_OF_MONTH));
            //viewHolder.dayText.setTypeface(typeface);

            //Hide any days that are not part of the current month to show
            if(day.get(Calendar.MONTH)!=monthToShow.get(Calendar.MONTH))
                viewHolder.outsideGridCell.setVisibility(INVISIBLE);
            else
                viewHolder.outsideGridCell.setVisibility(VISIBLE);

            //test if a day has been selected
            Calendar selectedDay;
            if(multiDaySelect) selectedDay = selectedDays[day.get(Calendar.DAY_OF_WEEK)-1]; //for the multiday cal, selected days are stored by their day of week
            else selectedDay = selectedDays[0];

            if(selectedDay != null){
                if(isSameDay(day,selectedDay)){
                    viewHolder.insideGridCell.setBackgroundColor(getResources().getColor(R.color.sessionsBlue));
                    viewHolder.dayText.setTextColor(getResources().getColor(android.R.color.white));
                }
                else {
                    viewHolder.insideGridCell.setBackgroundColor(getResources().getColor(android.R.color.white));
                    viewHolder.dayText.setTextColor(getResources().getColor(android.R.color.black));
                }

            }
            //if it is not the day then we must unselect it
            else {
                viewHolder.insideGridCell.setBackgroundColor(getResources().getColor(android.R.color.white));
                viewHolder.dayText.setTextColor(getResources().getColor(android.R.color.black));
            }

            //test if a day is todays day
            if(day.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
                    && day.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                    && day.get(Calendar.YEAR) == today.get(Calendar.YEAR)){
                viewHolder.currentDateCircle.setVisibility(VISIBLE);
                viewHolder.dayText.setTextColor(getResources().getColor(android.R.color.white));
            }
            else{
                viewHolder.currentDateCircle.setVisibility(INVISIBLE);
            }

            //TODO:Handle the event stickers so that users can see if they have something booked that day

        }

        @Override
        public int getItemCount(){ return this.daysOfMonth.size(); }

    }

    //Initializes and returns a dialog on which will be shown an overview of a day.
    //TODO: make everything except the dialog blurry
    private Dialog initOverviewDialog(){
        Dialog overviewDialog = new Dialog(getContext(), android.R.style.Theme_Light);
        overviewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        overviewDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        overviewDialog.setContentView(R.layout.calendar_overview_dialog);

        Window window = overviewDialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);

        dialogDate = (TextView) overviewDialog.findViewById(R.id.dialogDate);
        dialogDate.setTypeface(typeface);

        dialogList = (RecyclerView) overviewDialog.findViewById(R.id.upcomingEventsList);
        dialogList.setHasFixedSize(true);

        dialogList.setLayoutManager(new LinearLayoutManager(this.getContext()));

        dialogList.setAdapter(new OverviewListAdapter(this.sessions, this.getContext()));

        return overviewDialog;
    }

    //Refreshes the views of the overview dialog
    private void refreshOverviewDialog(Calendar date){

        String dateHeader;

        int dayOfMonth = date.get(Calendar.DAY_OF_MONTH);

        //Make sure adapter knows what day was selected
        OverviewListAdapter listAdapter = (OverviewListAdapter) dialogList.getAdapter();
        listAdapter.setDateClicked(date);

        String monthYearPattern = "MMMM yyyy";     //pattern for month and year representation
        SimpleDateFormat sdf = new SimpleDateFormat(monthYearPattern);
        String monthYear = sdf.format(date.getTime());

        String weekPattern = "EEEE";    //pattern for week text representation
        sdf.applyPattern(weekPattern);
        String dayOfWeek = sdf.format(date.getTime());

        dateHeader = "" + dayOfWeek + "\n" + dayOfMonth + " " + monthYear;

        dialogDate.setText(dateHeader);

        listAdapter.notifyDataSetChanged();



    }

    //adapter for the listview
    class OverviewListAdapter extends RecyclerView.Adapter<OverviewListAdapter.ViewHolder>{
        //Preferably the ArrayList should be weeded so that only sessions from the month being shown are added
        private ArrayList<Session> sessions;
        private ArrayList<Session> sessionsToday;
        private Calendar dateClicked;

        private Context context;

        public class ViewHolder extends RecyclerView.ViewHolder {

            //all views to be held
            public ImageView sessionImg;
            public TextView sessionTitle;
            public TextView timeHour;
            public TextView timeMin;
            public TextView timeAMPM;

            public TextView numPeopleGoing;

            public ImageView creatorImg;

            public ViewHolder(View view){
                super(view);

                this.sessionImg = view.findViewById(R.id.session_overviewimg);

                this.sessionTitle = view.findViewById(R.id.session_title);

                this.timeHour = view.findViewById(R.id.little_time_hour);
                this.timeMin = view.findViewById(R.id.little_time_min);
                this.timeAMPM = view.findViewById(R.id.little_time_ampm);

                this.numPeopleGoing = view.findViewById(R.id.going_num_people);

                this.creatorImg = view.findViewById(R.id.creatorimg);
            }
        }

        //constructor, accepts a list of previewable information
        public OverviewListAdapter(ArrayList<Session> sessions, Context context){
            this.sessions = sessions;

            this.context = context;
        }

        @Override
        public OverviewListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            LayoutInflater inflater = LayoutInflater.from(this.context);
            View list = inflater.inflate(R.layout.calendar_overview_list_elem,parent,false);
            ViewHolder vh = new ViewHolder(list);
            vh.sessionTitle.setTypeface(typeface);
            vh.timeHour.setTypeface(typeface);
            vh.timeMin.setTypeface(typeface);
            vh.timeAMPM.setTypeface(typeface);
            vh.numPeopleGoing.setTypeface(typeface);
            return vh;
        }

        //TODO: Finish retrieving user pic and make overview only show up when user has session.
        @Override
        public void onBindViewHolder(ViewHolder holder, int position){
            this.sessionsToday = new ArrayList<Session>();
            for(Session sesh: sessions){
                //date stored in Month Day format
                String[] dayMonth = sesh.getSessionDate().split(" ");
                //Our current method of storing the month uses Jan as 1 and Dec as 12, whereas Java.Calendar uses Jan as 0 and Dec as 11, so decrement month
                Calendar date = new GregorianCalendar(dateClicked.get(Calendar.YEAR),Integer.parseInt(dayMonth[0])-1, Integer.parseInt(dayMonth[1]));
                if(isSameDay(date, dateClicked)) sessionsToday.add(sesh);
            }
            if(sessionsToday!=null && sessionsToday.size()>=1 ) {
                Session session = sessionsToday.get(position);

                if(session.getSessionName()!=null) holder.sessionTitle.setText(session.getSessionName());

                //time is in HH:MM format
                if(session.getSessionTime()!=null) {
                    String[] splitTime = session.getSessionTime().split(":");
                    String hour = splitTime[0];

                    holder.timeMin.setText(splitTime[1]);
                    if(Integer.parseInt(hour)<13) {
                        holder.timeHour.setText(hour+":");
                        holder.timeAMPM.setText("AM");
                    }
                    else{
                        hour = Integer.toString((Integer.parseInt(hour)-12));

                        holder.timeHour.setText(hour+":");
                        holder.timeAMPM.setText("PM");
                    }
                }

                //Get the image of the session's creator
                //TODO: actually make this fetch data from the user.
                if(session.getUser()!=null){
                    String userName = session.getUser();
                    holder.creatorImg.setImageDrawable(getResources().getDrawable(R.drawable.basketball));

                }

                //Get the image of the session's sport
                if(session.getSessionSport()!=null){
                    holder.sessionImg.setImageBitmap(getBitmap(session.getSessionSport()));

                    //this actually gives a better image quality to the icon
                    //holder.sessionImg.setImageDrawable(getResources().getDrawable(R.drawable.basketball));
                }

                //Set the number of people going to this event.
                if(session.getSessionNumUsers()!=0) holder.numPeopleGoing.setText(""+session.getSessionNumUsers());
            }
            //For testing purposes
            else{
                holder.sessionTitle.setText("None");
                holder.creatorImg.setImageBitmap(getBitmap("FeaturedGym"));
                holder.sessionImg.setImageBitmap(getBitmap("FeaturedGym"));
                holder.timeAMPM.setText("AM");
                holder.timeHour.setText("1");
                holder.timeMin.setText("00");
                holder.numPeopleGoing.setText("2");
            }
        }

        @Override
        public int getItemCount(){
            if(sessions!=null) return sessions.size();
            else return 0;
        }

        public void setDateClicked(Calendar dateClicked){this.dateClicked = dateClicked;}

    }

    //Reinitialize the array of selections.
    private void reinitSelections(){
        for(int i = 0; i<this.selectedDays.length; i++){
            this.selectedDays[i] = null;
        }
    }


    //To compare if two calendar dates are referring to the same date.
    private boolean isSameDay(Calendar c1, Calendar c2){
        //we dont want no nulls
        if(c1==null || c2==null) return false;
        if(c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH)
                && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
                && c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
            return true;
        else
            return false;
    }

    //To obtain the dates selected on the calendar.
    //In the case of a single select calendar, the date will be in position 0 of the array.
    public Calendar[] getDates(){
        return this.selectedDays.clone();
    }

    public void setTypeface(Typeface typeface){
        this.typeface = typeface;
    }

    public void setSessionSet(ArrayList<Session> sessionSet) { this.sessions = sessionSet;}

    public void setObserver(CustomCalendarView.SelectionObserver observer) {this.selectionObserver = observer;}

    public void setSelectedDays(Calendar[] selectedDays){ this.selectedDays = selectedDays;}

    public void setSelectedPos(int selectedPos){ this.posSelected = selectedPos;}

    //to make sure a fragment stored by the viewpager is being updated
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser){
        super.setUserVisibleHint(isVisibleToUser);

        //redraws the cell that was previously selected on this fragment.
        if(isVisibleToUser && this.customCalendar != null) this.customCalendar.getAdapter().notifyItemChanged(posSelected);
    }

    //To generate the ArrayList containing the different days of the month to show
    public ArrayList<String> makeDaySet(int offset){
        Calendar monthToShow = new GregorianCalendar();
        monthToShow.add(Calendar.MONTH,offset);
        ArrayList<String> daysOfMonth = new ArrayList<String>();
        Calendar tmpCal = (Calendar) monthToShow.clone();

        tmpCal.set(Calendar.DAY_OF_MONTH,1);    //go back to beginning of month
        int firstGridOffset = tmpCal.get(Calendar.DAY_OF_WEEK)-1;

        tmpCal.add(Calendar.DAY_OF_MONTH, -firstGridOffset);

        //fill ArrayList with 42 days of the calendar
        while(daysOfMonth.size()<42){
            String dayInfo = tmpCal.get(Calendar.DAY_OF_MONTH)+"/"+tmpCal.get(Calendar.MONTH)+"/"+
                    tmpCal.get(Calendar.YEAR);
            //daysOfMonth.add((Calendar)tmpCal.clone());
            daysOfMonth.add(dayInfo);

            tmpCal.add(Calendar.DAY_OF_MONTH,1);
        }

        return daysOfMonth;
    }

    //TODO: Remove this code when integrating into app
    public Bitmap getBitmap(String sport){

        Bitmap imageBitmap;
        Bitmap resizedBitmap = null;
        switch(sport){

            case "Basketball":
                imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.basketball);
                resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 60, 60, false);
                break;
            case "Boxing":
                imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.boxing);
                resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 60, 60, false);
                break;
            case "Football":
                imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.football);
                resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 60, 60, false);
                break;
            case "Gym":
                imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gym);
                resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 60, 60, false);
                break;
            case "Running":
                imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.running);
                resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 60, 60, false);
                break;
            case "Soccer":
                imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.soccer);
                resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 100, 100, false);
                break;
            case "Swimming":
                imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.swimming);
                resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 60, 60, false);
                break;
            case "Tennis":
                imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tennis);
                resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 60, 60, false);
                break;
            case "Volleyball":
                imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.volleyball);
                resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 60, 60, false);
                break;
            case "FeaturedGym":
                imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.featured_gym);
                resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 150, 150, false);
                break;
            case "Other":
                imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.marker);
                resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 60, 60, false);
                break;

        }

        return resizedBitmap;
    }


}

