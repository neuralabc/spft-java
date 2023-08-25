resample_freq = 500; %need to know the resample frequency, since this determines where our times are
triggers = [EEG.event.latency];
triggers = triggers(2:end); %skip the first one, since it appears to be a dummy value

%check if we have the correct number of triggers
num_triggers = 3*3*5*2; %nine 3 trials for each block, 3 sequences (LRN,SMP,RST), 2 events per trial (start,stop)

%if this is true, then we are good to go!
numel(triggers) == num_triggers

%%
triggers = triggers./resample_freq*1000; %to put into same time space as "times", divide by freq and convert to ms
times = EEG.times;

trigger_idxs = [];
for i = 1:length(triggers)
    index = find(times >=triggers(i) ,1);
    trigger_idxs = [trigger_idxs,index];
end


trigger_idxs_on = trigger_idxs(1:2:end);
trigger_idxs_off = trigger_idxs(2:2:end);
%%
%assuming LRN, SMP, RST, with 3 trials each and 5 blocks
LRN_idx= [1:3,[1:3]+9*1,[1:3]+9*2,[1:3]+9*3,[1:3]+9*4]; 
LRN_on = trigger_idxs_on(LRN_idx);
LRN_off = trigger_idxs_off(LRN_idx);
SMP_on = trigger_idxs_on(LRN_idx+3);
SMP_off = trigger_idxs_off(LRN_idx+3);
RST_on = trigger_idxs_on(LRN_idx+6);
RST_off = trigger_idxs_off(LRN_idx+6);

%% Concatenate trial data from EEG and EMG before computing coherence, 
raw_d = EEG.data(5,:); %specify the data that you are going to work with (5 is c3 in our example data)
for i=1:length(SMP_on)
  temp_d2 = raw_d(SMP_on(i):SMP_off(i));
  if i == 1
    temp_d = temp_d2;
  else
    temp_d = horzcat(temp_d,temp_d2);
  end
end
temp_d = temp_d./length(SMP_on);
C3_SMP_vec = temp_d;

for i=1:length(LRN_on)
  temp_d2 = raw_d(LRN_on(i):LRN_off(i));
  if i == 1
    temp_d = temp_d2;
  else
    temp_d = horzcat(temp_d,temp_d2);
  end
end
temp_d = temp_d./length(LRN_on);
C3_LRN_vec = temp_d;

for i=1:length(RST_on)
  temp_d2 = raw_d(RST_on(i):RST_off(i));
  if i == 1
    temp_d = temp_d2;
  else
    temp_d = horzcat(temp_d,temp_d2);
  end
end
temp_d = temp_d./length(RST_on);
C3_RST_vec = temp_d;
%%

%same thing, but for EMG data
raw_d = [] %specify the EMG data here
for i=1:length(SMP_on)
  temp_d2 = raw_d(SMP_on(i):SMP_off(i));
  if i == 1
    temp_d = temp_d2;
  else
    temp_d = horzcat(temp_d,temp_d2);
  end
end
temp_d = temp_d./length(SMP_on);
EMG1_SMP_vec = temp_d;

for i=1:length(LRN_on)
  temp_d2 = raw_d(LRN_on(i):LRN_off(i));
  if i == 1
    temp_d = temp_d2;
  else
    temp_d = horzcat(temp_d,temp_d2);
  end
end
temp_d = temp_d./length(LRN_on);
EMG1_LRN_vec = temp_d;

for i=1:length(RST_on)
  temp_d2 = raw_d(RST_on(i):RST_off(i));
  if i == 1
    temp_d = temp_d2;
  else
    temp_d = horzcat(temp_d,temp_d2);
  end
end
temp_d = temp_d./length(RST_on);
EMG1_RST_vec = temp_d;


%% OLDER STUFF -- Compute average per trial (for testing only)
%create average for SMP trials
temp_d = zeros(1,8762); %THIS IS JUST FOR TESTING!
for i=1:length(SMP_on)
  temp_d2 = EEG.data(5,SMP_on(i):SMP_off(i));
  disp(length(temp_d2))
  temp_d = temp_d + temp_d2(1:8762);
end
temp_d = temp_d./length(SMP_on);
SMP_avg = temp_d;

%now for LRN trials
temp_d = zeros(1,8762);%THIS IS JUST FOR TESTING
for i=1:length(LRN_on)
  temp_d2 = EEG.data(5,LRN_on(i):LRN_off(i));
  disp(length(temp_d2))
  temp_d = temp_d + temp_d2(1:8762);
end
temp_d = temp_d./length(LRN_on);
LRN_avg = temp_d;

%now for RST trials
temp_d = zeros(1,8762);%THIS IS JUST FOR TESTING
for i=1:length(RST_on)
  temp_d2 = EEG.data(5,RST_on(i):RST_off(i));
  disp(length(temp_d2))
  temp_d = temp_d + temp_d2(1:8762);
end
temp_d = temp_d./length(RST_on);
RST_avg = temp_d;


%% EMG
%initial code for EMG extraction, we assume the timecodes are the same
% BUT we should likely check
emg_1 = ALLEEG(2).data(1,:);

%create emg average for SMP trials
temp_d = zeros(1,8762); %THIS IS JUST FOR TESTING!
for i=1:length(SMP_on)
  temp_d2 = emg_1(SMP_on(i):SMP_off(i));
  disp(length(temp_d2))
  temp_d = temp_d + temp_d2(1:8762);
end
temp_d = temp_d./length(SMP_on);
emg_1_SMP_avg = temp_d;


emg_2 = ALLEEG(2).data(2,:);
%create emg average for SMP trials
temp_d = zeros(1,8762); %THIS IS JUST FOR TESTING!
for i=1:length(SMP_on)
  temp_d2 = emg_2(SMP_on(i):SMP_off(i));
  disp(length(temp_d2))
  temp_d = temp_d + temp_d2(1:8762);
end
temp_d = temp_d./length(SMP_on);
emg_2_SMP_avg = temp_d;
%%
