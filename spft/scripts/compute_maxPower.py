'''
code to calculate the max power of a signal, from here (https://stackoverflow.com/questions/58193209/function-to-determine-the-frequency-of-a-sinusoid)
this relates to the SPFT sequences, which have max power at 0.52 Hz
'''

import numpy as np
from matplotlib import pyplot as plt


d = #your data vector [...]
fs = 1000/12 #sampling frequency (of your signal, of course)
y_fft = np.fft.fft(d-np.mean(d))           # Original FFT, offset removed
y_fft = y_fft[:round(len(d)/2)] # First half ( pos freqs )
y_fft = np.abs(y_fft)           # Absolute value of magnitudes
y_fft = y_fft/max(y_fft)        # Normalized so max = 1

freq_x_axis = np.linspace(0, fs/2, len(y_fft))
plt.figure()
plt.plot(freq_x_axis, y_fft, "o-")
plt.title("Frequency magnitudes")
plt.xlabel("Frequency")
plt.ylabel("Magnitude")
plt.grid()
plt.tight_layout()
plt.show()

f_loc = np.argmax(y_fft) # Finds the index of the max
f_val = freq_x_axis[f_loc] # The strongest frequency value
print(f"The strongest frequency is f = {f_val}")
