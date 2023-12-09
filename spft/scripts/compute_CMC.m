
%function all_trials_vec = extract_trial_data(vec_data, starts, stops, num_samples)
%  for i=1:length(starts) %taking 1 to 15 in the FOR loop since there are 15 trials of SMP
%    trial_vec = raw_d(starts(i):stops(i)); %temp_d2 gets the raw eeg values for SMP trial 1, and so on
%    trial_vec = trial_vec(end-num_samples+1:end); %skipping some at start
%    %trial_vec = trial_vec(1:num_samples); %skipping some at end
%    
%    if i == 1 %this condition will only satisfy the first FOR loop - tranfering the values from temp_d2 to temp_d which will further get concatenated
%      all_trials_vec = trial_vec;
%    else
%      all_trials_vec = horzcat(all_trials_vec,trial_vec); %concatenate trials together
%    end
%  end
%end

% identify electrodes of interest on EEG
% eeglab processed data stored in structure: EEG.data
% EEG.chanlocs gives the lookup for channel names to rows in EEG.data
%      FC3
%C5  C3  C1
%      CP3

c3 = ALLEEG(1).data(5,:); %all data from the 5th channel
fc3 = ALLEEG(1).data(40,:);
c5 = ALLEEG(1).data(48,:);
c1 = ALLEEG(1).data(34,:);
cp3 = ALLEEG(1).data(42,:);
 %laplacian rereferencing for channel C3
lc3 = c3 - (fc3+c5+c1+cp3)/4;

% identify EMG
% now load EMG, THIS CODE DOES NOT WORK CORRECTLY IF YOU LOAD IN ANOTHER
% ORDER
% we added it to the ALLEEG structure, as ALLEEG(2) (.data)
emg_1 = ALLEEG(2).data(1,:);
emg_2 = ALLEEG(2).data(2,:);

%Split data into trials and concatenate before computing mscohere

% run coherence
%[Cxy,W] = mscohere(emg_1,lc3,hanning(500),0,512,500);

%Triggers%
resample_freq = 500; %need to know the resample frequency, since this determines where our times are
triggers = [EEG.event.latency];
triggers = triggers(2:end); %skip the first one, since it appears to be a dummy value

%check if we have the correct number of triggers
num_triggers = 3*3*5*2; % 3 trials for each block, 3 sequences (LRN,RST,SMP), 5 blocks, 2 events per trial (start,stop)

%if this is true, then we are good to go!
numel(triggers) == num_triggers

%
triggers = triggers./resample_freq*1000; %to put into same time space as "times", divide by freq and convert to ms
times = EEG.times; %already in ms

trigger_idxs = []; %empty array
for i = 1:length(triggers)
    index = find(times >=triggers(i) ,1); %loop will go on from 1 to 90; index carries the position where the times is more or equal to trigger.latency(which is in ms)
    trigger_idxs = [trigger_idxs,index]; %creating an array for to get positions where trigger happened in the eeg. Timewise we know where the trigger is, but not with respect to the datapoint
end


trigger_idxs_on = trigger_idxs(1:2:end); % total 90 triggers, start and end; demarking which the start ones
trigger_idxs_off = trigger_idxs(2:2:end); % demarking the end ones

%assuming LRN, SMP, RST, with 3 trials each and 5 blocks
LRN_idx= [1:3,[1:3]+9*1,[1:3]+9*2,[1:3]+9*3,[1:3]+9*4]; %empty array for positioning; duration for ON's and OFF's  
LRN_on = trigger_idxs_on(LRN_idx);
LRN_off = trigger_idxs_off(LRN_idx);
RST_on = trigger_idxs_on(LRN_idx+3);
RST_off = trigger_idxs_off(LRN_idx+3);
SMP_on = trigger_idxs_on(LRN_idx+6);
SMP_off = trigger_idxs_off(LRN_idx+6);

%triggers are initially placed to mark the start and end of each trial
% our experiment design includes 5 blocks with three trials of LRN, three
% trials of RST and three trials of SMP in this order
%the above chunk of code identifies the exact start and end of these three
%sequences and prepares the data for concatenation
%% concatenation of EEG and EMG data with trimming to minimum length
% first we look at how many elements there are for each trial so that we can trim them to the same length

raw_d = lc3; %specify the data that you are going to work with (5 is c3 in our example data)
for i=1:length(SMP_on) %taking 1 to 15 in the FOR loop since there are 15 trials of SMP
  temp_d2 = raw_d(SMP_on(i):SMP_off(i)); %temp_d2 gets the raw eeg values for SMP trial 1, and so on
  if i == 1 %this condition will only satisfy the first FOR loop - tranfering the values from temp_d2 to temp_d which will further get concatenated
    temp_d = temp_d2;
    a=length(temp_d2);
  else
    a = horzcat(a,length(temp_d2));
  end
end

plot(a)
num_samples = 8500 %based on the plot(a) information and our desired 500 window sample we take this value


%% incorporated this logic into the entire code
% basic idea is to trim the timeseries for each trial to meet the total num_samples, here we take it off of the front of the vector to 
%   hopefully control for any effects where participants are not responding immediately.
% this code below was incorporated into the rest of the code, left here for testing purposes
raw_d = lc3;
for i=1:length(SMP_on) %taking 1 to 15 in the FOR loop since there are 15 trials of SMP
  temp_d2 = raw_d(SMP_on(i):SMP_off(i));%temp_d2 gets the raw eeg values for SMP trial 1, and so on
  temp_d2 = temp_d2(end-num_samples+1:end); %skipping some at start
  %temp_d2 = temp_d2(1:num_samples); %skipping some at end
 
  if i == 1 %this condition will only satisfy the first FOR loop - tranfering the values from temp_d2 to temp_d which will further get concatenated
    temp_d = temp_d2;
  else
    temp_d = horzcat(temp_d,temp_d2); %concatenate SMP trials together
  end
end
C3_SMP_vec = temp_d; % final vector with all SMP trials 

%same thing, but for EMG data
raw_d = ALLEEG(2).data(1,:); %specify the EMG data here
for i=1:length(SMP_on)
  temp_d2 = raw_d(SMP_on(i):SMP_off(i));
  temp_d2 = temp_d2(end-num_samples+1:end); %skipping some at start
  %temp_d2 = temp_d2(1:num_samples); %skipping some at end
  if i == 1
    temp_d = temp_d2;
  else
    temp_d = horzcat(temp_d,temp_d2);
  end
end

EMG1_SMP_vec = temp_d;

figure
mscohere(EMG1_SMP_vec,C3_SMP_vec,hanning(500),0,512,500); % coherence for EMG1 and C3 SMP

%% as above, but instead we look at each trial independently
raw_d = lc3;
ons = RST_on;
offs = RST_off;
for i=1:length(SMP_on) %taking 1 to 15 in the FOR loop since there are 15 trials of SMP
  temp_d2 = raw_d(ons(i):offs(i));%temp_d2 gets the raw eeg values for SMP trial 1, and so on
  temp_d2 = temp_d2(end-num_samples+1:end); %skipping some at start
  %temp_d2 = temp_d2(1:num_samples); %skipping some at end
 
  if i == 1 %this condition will only satisfy the first FOR loop - tranfering the values from temp_d2 to temp_d which will further get concatenated
    temp_d = temp_d2;
  else
    temp_d = vertcat(temp_d,temp_d2); %concatenate SMP trials together
  end
end
C3_SMP_vec = temp_d; % final vector with all SMP trials 

%same thing, but for EMG data
raw_d = ALLEEG(2).data(1,:); %specify the EMG data here
for i=1:length(SMP_on)
  temp_d2 = raw_d(ons(i):offs(i));
  temp_d2 = temp_d2(end-num_samples+1:end); %skipping some at start
  %temp_d2 = temp_d2(1:num_samples); %skipping some at end
  if i == 1
    temp_d = temp_d2;
  else
    temp_d = vertcat(temp_d,temp_d2);
  end
end

EMG1_SMP_vec = temp_d;

% figure
% for i=1:length(SMP_on)
%     [C,W] = mscohere(EMG1_SMP_vec(i,:),C3_SMP_vec(i,:),hanning(500),0,512,500);
%     plot(W, C, "DisplayName",string(i));
%     if i == 1
%         hold on
%         all_C = C';
%     else
%         all_C = vertcat(all_C,C');
%     end
% end
% hold off

%wavelet coherence
% for i=1:length(SMP_on)
%     figure
%     wcoherence(EMG1_SMP_vec(i,:),C3_SMP_vec(i,:),500,'FrequencyLimits',[0.25,50]);
%     title(string(i))
% end

figure
wcoherence(mean(EMG1_SMP_vec,1),mean(C3_SMP_vec,1),500,'FrequencyLimits',[0.25,50]);
title('mean wavelet coherence')

% figure
% plot(W,mean(all_C,1))

%% Concatenate trial data from EEG and EMG before computing coherence, 
%--------------------------------------------------------------------------------------------------------------
% start w/ the EEG data
%--------------------------------------------------------------------------------------------------------------

raw_d = lc3; %specify the data that you are going to work with (5 is c3 in our example data)
for i=1:length(SMP_on) %taking 1 to 15 in the FOR loop since there are 15 trials of SMP
  temp_d2 = raw_d(SMP_on(i):SMP_off(i)); %temp_d2 gets the raw eeg values for SMP trial 1, and so on
  temp_d2 = temp_d2(end-num_samples+1:end); %skipping some at start
  %temp_d2 = temp_d2(1:num_samples); %skipping some at end
  
  if i == 1 %this condition will only satisfy the first FOR loop - tranfering the values from temp_d2 to temp_d which will further get concatenated
    temp_d = temp_d2;
  else
    temp_d = horzcat(temp_d,temp_d2); %concatenate SMP trials together
  end
end

C3_SMP_vec = temp_d; % final vector with all SMP trials 

for i=1:length(LRN_on)
  temp_d2 = raw_d(LRN_on(i):LRN_off(i));
  temp_d2 = temp_d2(end-num_samples+1:end); %skipping some at start
  %temp_d2 = temp_d2(1:num_samples); %skipping some at end
  if i == 1
    temp_d = temp_d2;
  else
    temp_d = horzcat(temp_d,temp_d2);
  end
end

C3_LRN_vec = temp_d;

for i=1:length(RST_on)
  temp_d2 = raw_d(RST_on(i):RST_off(i));
  temp_d2 = temp_d2(end-num_samples+1:end); %skipping some at start
  %temp_d2 = temp_d2(1:num_samples); %skipping some at end
  if i == 1
    temp_d = temp_d2;
  else
    temp_d = horzcat(temp_d,temp_d2);
  end
end

C3_RST_vec = temp_d;

%% Now the EMG data concatenation
%--------------------------------------------------------------------------------------------------------------
%same thing, but for the first EMG data channel
%--------------------------------------------------------------------------------------------------------------
raw_d = emg_1; %specify the EMG data here
for i=1:length(SMP_on)
  temp_d2 = raw_d(SMP_on(i):SMP_off(i));
  temp_d2 = temp_d2(end-num_samples+1:end); %skipping some at start
  %temp_d2 = temp_d2(1:num_samples); %skipping some at end
  if i == 1
    temp_d = temp_d2;
  else
    temp_d = horzcat(temp_d,temp_d2);
  end
end

EMG1_SMP_vec = temp_d;

for i=1:length(LRN_on)
  temp_d2 = raw_d(LRN_on(i):LRN_off(i));
  temp_d2 = temp_d2(end-num_samples+1:end); %skipping some at start
  %temp_d2 = temp_d2(1:num_samples); %skipping some at end
  if i == 1
    temp_d = temp_d2;
  else
    temp_d = horzcat(temp_d,temp_d2);
  end
end

EMG1_LRN_vec = temp_d;

for i=1:length(RST_on)
  temp_d2 = raw_d(RST_on(i):RST_off(i));
  temp_d2 = temp_d2(end-num_samples+1:end); %skipping some at start
  %temp_d2 = temp_d2(1:num_samples); %skipping some at end
  if i == 1
    temp_d = temp_d2;
  else
    temp_d = horzcat(temp_d,temp_d2);
  end
end

EMG1_RST_vec = temp_d;

%--------------------------------------------------------------------------------------------------------------
%now for the second EMG channel
%--------------------------------------------------------------------------------------------------------------
raw_d = emg_2; %specify the EMG data here
for i=1:length(SMP_on)
  temp_d2 = raw_d(SMP_on(i):SMP_off(i));
  temp_d2 = temp_d2(end-num_samples+1:end); %skipping some at start
  %temp_d2 = temp_d2(1:num_samples); %skipping some at end
  if i == 1
    temp_d = temp_d2;
  else
    temp_d = horzcat(temp_d,temp_d2);
  end
end
EMG2_SMP_vec = temp_d;

for i=1:length(LRN_on)
  temp_d2 = raw_d(LRN_on(i):LRN_off(i));
  temp_d2 = temp_d2(end-num_samples+1:end); %skipping some at start
  %temp_d2 = temp_d2(1:num_samples); %skipping some at end
  if i == 1
    temp_d = temp_d2;
  else
    temp_d= horzcat(temp_d,temp_d2);
  end
end

EMG2_LRN_vec = temp_d;

for i=1:length(RST_on)
  temp_d2 = raw_d(RST_on(i):RST_off(i));
  temp_d2 = temp_d2(end-num_samples+1:end); %skipping some at start
  %temp_d2 = temp_d2(1:num_samples); %skipping some at end
  if i == 1
    temp_d = temp_d2;
  else
    temp_d = horzcat(temp_d,temp_d2);
  end
end

EMG2_RST_vec = temp_d;

%--------------------------------------------------------------------------------------------------------------
%% compute cherence and spectral density and plot
%--------------------------------------------------------------------------------------------------------------
% Now, all trials of LRN and SMP sequences throughout the task are concatenated
% coherence, with following params: mscohere( sig1, sig2, window, overlap, nfft(sampling points), frequency)

[Cxy,W] = mscohere(EMG1_SMP_vec,C3_SMP_vec,hanning(500),0,512,500); % coherence for EMG1 and C3 SMP
[Cxz,X] = mscohere(EMG2_SMP_vec,C3_SMP_vec,hanning(500),0,512,500); % coherence for EMG2 and C3 SMP
[Cxw,Y] = mscohere(EMG1_LRN_vec,C3_LRN_vec,hanning(500),0,512,500); % coherence for EMG1 and C3 LRN
[Cxx,Z] = mscohere(EMG2_LRN_vec,C3_LRN_vec,hanning(500),0,512,500); % coherence for EMG2 and C3 LRN

%plot(W, Cxy) %coherence plot for EMG1 and C3 for SMP
%plot(X,Cxz) % coherence plot for EMG2 and C3 SMP
%plot(Y, Cxw) % coherence plot for EMG1 and C3 LRN
%plot(Z, Cxx) % coherence plot for EMG2 and C3 LRN

figure
plot(W, Cxy, "red", "DisplayName","EMG1 and C3 SMP");
hold on
plot(X, Cxz, "blue", "DisplayName", "EMG2 and C3 SMP");
hold on
plot(Y, Cxw, "green", "DisplayName", "EMG1 and C3 LRN");
hold on
plot(Z, Cxx, "black", "DisplayName", "EMG2 and C3 LRN");
hold off
legend

wcoherence(EMG1_SMP_vec,C3_SMP_vec,500)
%% spectral density
periodogram(lc3(1:18000),rectwin(18*1000),18*1000,500)
%periodogram(lc3(1:18000),rectwin(18*1000),18*1000,500)
periodogram(lc3(end-18000:end),rectwin(18*1000),18*1000,500)
%periodogram(lc3(end-18001:end),rectwin(18*1000),18*1000,500)
