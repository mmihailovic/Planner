package com.example.myapplication.view.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.myapplication.R;
import com.example.myapplication.models.Day;
import com.example.myapplication.models.Obaveza;
import com.example.myapplication.view.MainActivity;
import com.example.myapplication.view.recycler.adapter.ObavezaAdapter;
import com.example.myapplication.view.recycler.differ.ObavezaItemDiffCallback;
import com.example.myapplication.viewmodels.ObavezaRecyclerViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import timber.log.Timber;

public class DailyPlanFragment extends Fragment {
    private RecyclerView recyclerView;
    private ObavezaRecyclerViewModel recyclerViewModel;
    private TextView dateTextView;
    private CheckBox checkBoxPastObligations;
    //private boolean pastObligations = false;
    private SearchView searchView;
    private FloatingActionButton floatingActionButton;
    private ToggleButton low;
    private ToggleButton mid;
    private ToggleButton high;

    private ObavezaAdapter obavezeAdapter;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd. yyyy.");

    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String date;

    private Day day;

    private String searchString;

    public DailyPlanFragment() {
        super(R.layout.fragment_daily_plan);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerViewModel = new ViewModelProvider(this).get(ObavezaRecyclerViewModel.class);
        ((MainActivity)this.getActivity()).setDailyPlanViewModel(recyclerViewModel);
        init(view);
        //view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void init(View view) {
        initView(view);
        initListeners(view);
        initObservers();
        initRecycler();
    }

    private void initView(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        dateTextView = view.findViewById(R.id.dateTextView);
        checkBoxPastObligations = view.findViewById(R.id.checkBoxPastObligations);
        searchView = view.findViewById(R.id.searchView);
        floatingActionButton = view.findViewById(R.id.floatingActionButtonAdd);
        low = view.findViewById(R.id.btnLow);
        mid = view.findViewById(R.id.btnMid);
        high = view.findViewById(R.id.btnHigh);
        low.setAlpha(0.5f);
        mid.setAlpha(0.5f);
        high.setAlpha(0.5f);


        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MMM dd. yyyy.");
        date = dateFormat.format(calendar.getTime());
        dateTextView.setText(date);
    }

    private void initListeners(View view) {
        floatingActionButton.setOnClickListener(e->{
            FragmentTransaction transaction = this.getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.replaceFragmentFcv, new CreateObavezaFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });
        checkBoxPastObligations.setOnClickListener(e->{
            filter();
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Ova    metoda se poziva kada korisnik klikne na dugme "Search" na tastaturi
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchString = newText;
                filter();
                return true;
            }
        });
        high.setOnClickListener(e->{
            if(!high.isChecked())
                high.setAlpha(0.5f);
            else high.setAlpha(1f);
            filter();
        });
        mid.setOnClickListener(e->{
            if(!mid.isChecked())
                mid.setAlpha(0.5f);
            else mid.setAlpha(1f);
            filter();
        });
        low.setOnClickListener(e->{
            if(!low.isChecked())
                low.setAlpha(0.5f);
            else low.setAlpha(1f);
            filter();
        });
    }
    private void initRecycler() {
        obavezeAdapter = new ObavezaAdapter(new ObavezaItemDiffCallback(), car -> {
            MainActivity mainActivity = ((MainActivity)this.getActivity());

            FragmentTransaction transaction = mainActivity.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.replaceFragmentFcv, new DetailsFragment());
            transaction.addToBackStack(null);
            transaction.commit();

//            ObavezaEditRecyclerViewModel viewModel = (ObavezaEditRecyclerViewModel) mainActivity.getObavezaEditViewModel();
//            ObavezaRecyclerViewModel obavezaRecyclerViewModel = (ObavezaRecyclerViewModel) mainActivity.getDailyPlanViewModel();
//            List<Obaveza> listToSubmit = new ArrayList<>(obavezaRecyclerViewModel.getCarList());
//            Log.d("Edit recycler view", String.valueOf(viewModel));
//            viewModel.setObaveze(listToSubmit);
////            viewModel.setDate(car.getDate());
//            viewModel.setCarList((ArrayList<Obaveza>) listToSubmit);
        },recyclerViewModel);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        recyclerView.setAdapter(obavezeAdapter);
    }
    private void initObservers() {
        recyclerViewModel.getObaveze().observe(this, cars -> {
            Log.d("Observer","brisanje");
            obavezeAdapter.submitList(cars);
        });
        recyclerViewModel.getDate().observe(this, date->{
            dateTextView.setText(recyclerViewModel.getDate().getValue().format(formatter));
        });
    }

    private void filter() {
        List<Obaveza> obaveze = recyclerViewModel.getCarList();
        List<Obaveza> filteredObaveza = new ArrayList<>();
        for(Obaveza o: obaveze) {
            boolean priorityCheck = true;
            boolean textCheck = true;
            boolean timeCheck = true;
            if(!high.isChecked() && !mid.isChecked() && !low.isChecked())
                priorityCheck = true;
            else {
                if((o.getPriority() == Obaveza.LOW && low.isChecked()) ||
                        (o.getPriority() == Obaveza.MID && mid.isChecked()) ||
                        (o.getPriority() == Obaveza.HIGH && high.isChecked()))
                    priorityCheck = true;
                else priorityCheck = false;
            }
            Timber.e(searchString);
            if(searchString != null && searchString.length() > 0) {
                if(!o.getTitle().toLowerCase().contains(searchString.toLowerCase()))
                    textCheck = false;
            }
            if(!checkBoxPastObligations.isChecked()) {
                if(LocalDate.now().isBefore(recyclerViewModel.getDate().getValue()) || (LocalDate.now().equals(recyclerViewModel.getDate().getValue()) && LocalTime.now().compareTo(LocalTime.parse(o.getEnd())) == -1)){
                    timeCheck = true;
                }
                else timeCheck = false;
            }
            Timber.e("priorityCheck " + priorityCheck + " timeCheck " + timeCheck + " textCheck " + textCheck);
            if(priorityCheck && textCheck && timeCheck)
                filteredObaveza.add(o);
        }
        recyclerViewModel.setObaveze(filteredObaveza);
    }


    public void setDay(Day day) {
        this.day = day;
    }
}