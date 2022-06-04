from statistics import mode
import yaml
import numpy as np


##
# yaml data storage notes:
# blocks = data['blocks']
# input data 1st block 1st trial (left): blocks[0]['trials'][0]['leftReference']
#       times,values
# block start time @ startTimestamp, end @ endTimestamp
##

MVC = data['maximumLeftVoluntaryContraction']
t_time = np.array(blocks[0]['trials'][0]['leftReference']['times'])
t_vals = np.array(blocks[0]['trials'][0]['leftReference']['values'])

#all times from the device over the course of the experiment
resp = data['devices'][0]
all_f_time = np.array(resp['times'])
all_f_vals = compute_normalized_force_response(np.array(resp['values']),MVC)

#loop this to identify the start and end times of the trials
start = t_time[0]
end = t_time[-1]

f_trial_mask = (all_f_time-start >= 0) & (all_f_time-end<=0)
f_time = all_f_time[f_trial_mask]
f_vals = all_f_vals[f_trial_mask]

t_vals_interp = np.interp(f_time,t_time,t_vals) #linear (piece-wise) interpolation of presented target bar positions into the actual response, now we can subtract directly

trial_rmse = np.sqrt(np.mean((t_vals_interp-f_vals)**2)) #root mean squared error #TOOD: have someone confirm algo
trial_sse = ((t_vals_interp-f_vals)**2).sum()

def load_yaml(fname):
    with open(fname, 'r') as file:
        data = yaml.safe_load(file)
    return data

def parse_yaml_output(fname, output_keys = ['blocks', 'devices', 'triggers']):
    """
    Parse output file (yaml formatted) from spft-java
    Leave output_keys as default unless you really know what you are doing!
    """
    #TODO: identify if some keys are missing - likely not necessary given construction of yaml output

    #these are the keys for where the output data is stored
    # all timeseries data stored as 'times' and 'values'
    ## blocks = data from what was presented in this session, including metadata, presentation heights, and timestamps
    ## devices = raw device data and timestamps
    ## triggers = time and values of triggers
    yaml_data = load_yaml(fname)
    all_keys = yaml_data.keys()
    data = {}
    for key in all_keys:
        if key not in output_keys:
            data[key] = yaml_data[key]
    ## now we can do something with the input data as necessary
    ## TODO: PROCESSING!
    return data

def compute_normalized_force_response(device_force_values, MVC, forceRangeMin=0.05, forceRangeMax=0.30, clamp=None):
    """
    Convert from sensor values to heigt values that were used in the display for direct comparison to the target sequence. Works fine for any array.
    ((deviceForceValue / MVC) - forceRangeMin)/(forceRangeMax-forceRangeMin)

    MVC:            maximumum voluntary contraction of this individual (in original device units)
    clamp:          list of min/max values to clamp any values that are above or below to, default = None (no clamping)
    """

    norm_resp = ((device_force_values / MVC) - forceRangeMin)/(forceRangeMax-forceRangeMin)
    if clamp is not None:
        norm_resp[norm_resp<clamp[0]] = clamp[0]
        norm_resp[norm_resp>clamp[1]] = clamp[1]
    return norm_resp

def compute_temporal_lag(response_height, target_height):
    """
    Compute the cross-correlation between two signals that vary over time (response_height and target_height)
    response_height:    participant response in normalized units
    target_height:      target sequence
    """

# https://stackoverflow.com/questions/13826290/estimating-small-time-shift-between-two-time-series
# import numpy as np
# from scipy.interpolate import interp1d
# from scipy.optimize import leastsq

# def yvals(x):
#     return np.sin(x)+np.sin(2*x)+np.sin(3*x)

# dx = .1
# X = np.arange(0,2*np.pi,dx)
# Y = yvals(X)

# unknown_shift = np.random.random() * dx
# Y_shifted = yvals(X + unknown_shift)

# def err_func(p):
#     return interp1d(X,Y)(X[1:-1]+p[0]) - Y_shifted[1:-1]

# p0 = [0,] # Inital guess of no shift
# found_shift = leastsq(err_func,p0)[0][0]

# print "Unknown shift: ", unknown_shift
# print "Found   shift: ", found_shift

#this works fine, still in discrete units but could parameterize the number that we
# interp to to sub-sample the units as necessary (precision > 80Hz)
# from scipy import signal

# yy = interp1d(np.arange(Y.size),Y)(np.arange(0,Y.size-1,.01))
# yy_shifted = interp1d(np.arange(Y_shifted.size),Y_shifted)(np.arange(0,Y_shifted.size-1,.01))
# xcorr = signal.correlate(yy,yy_shifted,mode='full')
# lags = signal.correlation_lags(yy.size,yy_shifted.size,mode='full')
# lag = lags[np.argmax(xcorr)]
# print(lag)
    num_trials = response_height.shape[0]
    xcorr_lag = np.zeros(num_trials,2)
    for trial_idx in np.arange(0,num_trials):
        xcorr = np.correlate(response_height[trial_idx],target_height,mode='full')
        mmax = np.argmax(xcorr)

