/*TODO:
- male speech, do parameters apply?
- Test nr of bands higher than 100?
- read the report again*/

(
var modFile, organFile, padFile, saxFile, celloFile, didgFile,fluteFile, carrFile;
var vocoderIntuitive, vocoderGUI;
var bass, sillyVoice, defaultB, sawCombination, dominator;
var workingDir = thisProcess.nowExecutingPath.dirname;
//https://www.best-speech-topics.com/sample-informative-speech.html : caffeine
//https://www.best-speech-topics.com/informative-speech-sample.html : humor
//https://www.best-speech-topics.com/fun-persuasive-speech.html : luck
modFile = Buffer.read(s, workingDir +/+ "Modulators/luck_speech.wav");
//https://freesound.org/people/hammondman/sounds/333747/
organFile = Buffer.read(s, workingDir +/+ "Carriers/organ_b2_modif.wav");
//https://freesound.org/people/ceich93/sounds/274558/
padFile = Buffer.read(s, workingDir +/+ "Carriers/pad_ab3_modif.wav");
//https://freesound.org/people/clruwe/sounds/121422/
saxFile = Buffer.read(s, workingDir +/+ "Carriers/sax_gb3_modif.wav");
//https://freesound.org/people/flcellogrl/sounds/195281/
celloFile = Buffer.read(s, workingDir +/+ "Carriers/cello_e2_modif.wav");
//https://freesound.org/people/InspectorJ/sounds/398272/
didgFile = Buffer.read(s, workingDir +/+ "Carriers/didgeridoo_a2_modif.wav");
//https://freesound.org/people/mrshoes/sounds/278175/
fluteFile = Buffer.read(s, workingDir +/+ "Carriers/panflute_d4_modif.wav");

//Vocoder code
vocoderIntuitive = {
	//Carrier must be a function otherwise does not work!
	arg  car = {Saw.ar(220)}, modulator = {PlayBuf.ar(2, modFile.bufnum, BufRateScale.kr(modFile.bufnum))},
	numBands = 20, outputMul = 25, minFreq = 100, maxFreq = 10000, bandSizeMult = 0.9;
	//function that apply one band of the vocoder
	{var vocoderStep = {
		arg bandwidth = 1000, freq = 440, carrier = Saw.ar(220),
		modulator = PlayBuf.ar(2, modFile.bufnum, BufRateScale.kr(modFile.bufnum));
		var filtMod = BPF.ar(modulator, freq, bandwidth/freq);
		var filtAmpl = Amplitude.kr(filtMod);
		var filtCarrier = BPF.ar(carrier, freq, bandwidth/freq);
		filtCarrier * filtAmpl;
	};

	//Logarithmic scale
	var step = (maxFreq/minFreq)**numBands.reciprocal;
	var curFreq = minFreq;
	var bands = Array.newClear(numBands);

	//Compute all bands in an array
	for(1, numBands,
		{arg i;
			bands.put(i-1, vocoderStep.value((curFreq*step - curFreq)*bandSizeMult, (curFreq + curFreq*step)/2, car, modulator));
			curFreq = curFreq * step;
	});

	//Mix everything
	outputMul * Mix.new(bands)};
};

//Some sounds definitions

//The three following sounds come from the follwing github repositories.
//They were adapted from SynthDef to function.
//https://github.com/brunoruviaro/SynthDefs-for-Patterns
bass = { |freq = 440, gate = 1, amp = 0.5, slideTime = 0.17, ffreq = 1100, width = 0.15, detune = 1.005, preamp = 4|
    var sig, env;
	env = Env.adsr(0.01, 0.3, 0.4, 0.1);
    freq = Lag.kr(freq, slideTime);
    sig = Mix(VarSaw.ar([freq, freq * detune], 0, width, preamp)).distort;
	sig = sig * amp * EnvGen.kr(env, gate, doneAction: 2);
    sig = LPF.ar(sig, ffreq);
    sig ! 2
};

sillyVoice = { arg
	freq = 220,
	amp = 0.5,
	vibratoSpeed = 6,
	vibratoDepth = 4,
	vowel = 0,
	att = 0.01,
	rel = 0.1,
	lag = 1,
	gate = 1;

	var in, vibrato, env, va, ve, vi, vo, vu, snd;

	vibrato = SinOsc.kr(vibratoSpeed, mul: vibratoDepth);
	in = Saw.ar(Lag.kr(freq, lag) + vibrato);
	env = EnvGen.kr(Env.asr(att, 1, rel), gate, doneAction: 2);

	va = BBandPass.ar(
		in: in,
		freq: [ 600, 1040, 2250, 2450, 2750 ],
		bw: [ 0.1, 0.067307692307692, 0.048888888888889, 0.048979591836735, 0.047272727272727 ],
		mul: [ 1, 0.44668359215096, 0.35481338923358, 0.35481338923358, 0.1 ]);

	ve = BBandPass.ar(
		in: in,
		freq: [ 400, 1620, 2400, 2800, 3100 ] ,
		bw: [ 0.1, 0.049382716049383, 0.041666666666667, 0.042857142857143, 0.038709677419355 ],
		mul: [ 1, 0.25118864315096, 0.35481338923358, 0.25118864315096, 0.12589254117942 ]);

	vi = BBandPass.ar(
		in: in,
		freq: [ 250, 1750, 2600, 3050, 3340 ] ,
		bw: [ 0.24, 0.051428571428571, 0.038461538461538, 0.039344262295082, 0.035928143712575 ],
		mul: [ 1, 0.031622776601684, 0.15848931924611, 0.079432823472428, 0.03981071705535 ] );

	vo = BBandPass.ar(
		in: in,
		freq:[ 400, 750, 2400, 2600, 2900 ] ,
		bw: [ 0.1, 0.10666666666667, 0.041666666666667, 0.046153846153846, 0.041379310344828 ],
		mul: [ 1, 0.28183829312645, 0.089125093813375, 0.1, 0.01 ]);

	vu = BBandPass.ar(
		in: in,
		freq: [ 350, 600, 2400, 2675, 2950 ],
		bw: [ 0.11428571428571, 0.13333333333333, 0.041666666666667, 0.044859813084112, 0.040677966101695 ],
		mul: [ 1, 0.1, 0.025118864315096, 0.03981071705535, 0.015848931924611 ]);

	snd = SelectX.ar(Lag.kr(vowel, lag), [va, ve, vi, vo, vu]);
	snd = Mix.new(snd);
	snd!2 * env * amp
};

defaultB = { arg freq=440, amp=0.1, pan=0, gate=1;
    var z;
    z = LPF.ar(
            Mix.new(VarSaw.ar(freq + [0, Rand(-0.4,0.0), Rand(0.0,0.4)], 0, 0.3)),
            XLine.kr(Rand(4000,5000), Rand(2500,3200), 1)
        ) * Linen.kr(gate, 0.01, 0.7, 0.3, 2);
    Pan2.ar(z, pan, amp);
};

//http://sccode.org/1-4YS
sawCombination = {|freq, gate=1|
	var env = EnvGen.ar(Env.adsr(0.01,0.3,0.5,0.1), gate, doneAction:2);
	var snd = Saw.ar(freq!2);
	env*snd;
};

//http://mcld.co.uk/blog/2009/reverse-engineering-the-rave-hoover.html
dominator = { |freq=440, amp=0.1, gate=1|
    var midfreqs, son, vibamount;

    // Portamento:
    freq = freq.lag(0.2, 0.6);
    // you could alternatively try:
    //  freq = Ramp.kr(freq, 0.2);

    // vibrato doesn't fade in until note is held:
    vibamount = EnvGen.kr(Env([0,0,1],[0.0,0.4], loopNode:1), HPZ1.kr(freq).abs).poll;
    // Vibrato (slightly complicated to allow it to fade in):
    freq = LinXFade2.kr(freq, freq * LFPar.kr(3).exprange(0.98, 1.02), vibamount * 2 - 1);

    // We want to chorus the frequencies to have a period of 0.258 seconds
    // ie freq difference is 0.258.reciprocal == 3.87
    midfreqs = freq + (3.87 * (-2 .. 2));

    // Add some drift to the frequencies so they don't sound so digitally locked in phase:
    midfreqs = midfreqs.collect{|f| f + (LFNoise1.kr(2) * 3) };

    // Now we generate the main sound via Saw oscs:
    son = Saw.ar(midfreqs).sum
        // also add the subharmonic, the pitch-locked bass:
        + SinOsc.ar(freq * [0.25, 0.5, 0.75], 0, [1, 0.3, 0.2] * 2).sum;

    // As the pitch scoops away, we low-pass filter it to allow the sound to stop without simply gating it
    son = RLPF.ar(son, freq * if(freq < 100, 1, 32).lag(0.01));

    // Add a bit more mid-frequency emphasis to the sound
    son = son + BPF.ar(son, 1000, mul: 0.5) + BPF.ar(son, 3000, mul: 0.3);

    // This envelope mainly exists to allow the synth to free when needed:
    son = son * EnvGen.ar(Env.asr, gate, doneAction:2);

    Pan2.ar(son * amp)
};


//GUI definition
vocoderGUI = {
	//All variables have to be declared at the beginning of the function in supercollider
	var running, w, pause = true, carrier;
	var freqKnob, freqKnobLabel, freqKnobNr, freqMult = 2000, freqShift = 10;
	var bandsKnob, bandsKnobLabel, bandsKnobNr, bandsMult = 100, bandsShift = 1;
	var bandWidthKnob, bandWidthKnobLabel, bandWidthKnobNr, bandWidthMult = 1.0/1.01, bandWidthShift = 0.01;
	var volKnob, volKnobLabel, volKnobNr, volMult = 500, volShift = 0;
	var freqRangeSlider, lowFreqNr, highFreqNr, fRangePow = 20000, fRangeShift = 0, fRangeLabel;
	var carMenu, carMenuLabel, carFileButton, useListButton, userCarrFile = false;
	var onButton, offButton;
	var modulator = {PlayBuf.ar(2, modFile.bufnum, BufRateScale.kr(modFile.bufnum))};
	var modFileButton, modMenuLabel, micButton;

	w= Window("My tunable Vocoder",Rect(100,300,500,300));

	//Knob to tune frequency
	freqKnob= Knob(w);
	freqKnobNr = NumberBox(w);
	freqKnob.value = 0.2;
	freqKnob.action={
		//Stop the current audio if any
		if(pause == false,{ free(running); pause = true;});
		freqKnobNr.value = freqKnob.value*freqMult+freqShift;
	};

	freqKnobLabel = StaticText(w);
	freqKnobLabel.string = "Frequency";
	freqKnobLabel.align = \center;

	freqKnobNr.value = freqKnob.value*freqMult+freqShift;
	freqKnobNr.action = {if(pause == false,{ free(running); pause = true;});
		freqKnob.value = (freqKnobNr.value-freqShift)/freqMult;
	};

	//Knob to tune number of bands
	bandsKnob= Knob(w);
	bandsKnobNr = NumberBox(w);
	bandsKnob.action={if(pause == false,{ free(running); pause = true;});
		bandsKnobNr.value = round(bandsKnob.value*bandsMult+bandsShift);
	};
	bandsKnob.value = 0.5;

	bandsKnobLabel = StaticText(w);
	bandsKnobLabel.string = "Bands";
	bandsKnobLabel.align = \center;

	bandsKnobNr.value = round(bandsKnob.value*bandsMult+bandsShift);
	bandsKnobNr.action = {if(pause == false,{ free(running); pause = true;});
		bandsKnob.value = (bandsKnobNr.value-bandsShift)/bandsMult;
	};

	//Knob to tune band size (1 is the full band between filters, but as they are ButterWorth filter,
	//there is some overlap
	bandWidthKnob= Knob(w);
	bandWidthKnobNr = NumberBox(w);
	bandWidthKnob.action={if(pause == false,{ free(running); pause = true;});
		bandWidthKnobNr.value = bandWidthKnob.value*bandWidthMult+bandWidthShift;
	};
	bandWidthKnob.value = 0.8;

	bandWidthKnobLabel = StaticText(w);
	bandWidthKnobLabel.string = "Bands width factor";
	bandWidthKnobLabel.align = \center;

	bandWidthKnobNr.value = bandWidthKnob.value*bandWidthMult+bandWidthShift;
	bandWidthKnobNr.action = {if(pause == false,{ free(running); pause = true;});
		bandWidthKnob.value = (bandWidthKnobNr.value-bandWidthShift)/bandWidthMult;
	};

	//Knob to tune volume
	volKnob= Knob(w);
	volKnobNr = NumberBox(w);
	volKnob.action={if(pause == false,{ free(running); pause = true;});
		volKnobNr.value = volKnob.value*volMult+volShift;
	};
	volKnob.value = 0.05;

	volKnobLabel = StaticText(w);
	volKnobLabel.string = "Volume";
	volKnobLabel.align = \center;

	volKnobNr.value = volKnob.value*volMult+volShift;
	volKnobNr.action = {if(pause == false,{ free(running); pause = true;});
		volKnob.value = (volKnobNr.value-volShift)/volMult;
	};

	//Range slider for interval of frequencies values
	freqRangeSlider = RangeSlider(w);
	freqRangeSlider.action={if(pause == false,{ free(running); pause = true;});
		lowFreqNr.value = fRangePow**freqRangeSlider.lo+fRangeShift;
		highFreqNr.value = fRangePow**freqRangeSlider.hi+fRangeShift;
	};
	lowFreqNr = NumberBox(w);
	highFreqNr = NumberBox(w);

	lowFreqNr.value = fRangePow**freqRangeSlider.lo+fRangeShift;
	lowFreqNr.action = {if(pause == false,{ free(running); pause = true;});
		freqRangeSlider.lo = log(lowFreqNr.value-fRangeShift)/log(fRangePow);
	};

	highFreqNr.value = fRangePow**freqRangeSlider.hi+fRangeShift;
	highFreqNr.action = {if(pause == false,{ free(running); pause = true;});
		freqRangeSlider.hi = log(highFreqNr.value-fRangeShift)/log(fRangePow);
	};

	fRangeLabel = StaticText(w);
	fRangeLabel.string = "Highest frequency\n\nLowest frequency";
	fRangeLabel.align = \center;

	//Popup menu to select carrier sound
	carMenuLabel = StaticText(w);
	carMenuLabel.string = "Carrier Signal";
	carMenuLabel.align = \center;

	carMenu = PopUpMenu(w);

	carMenu.items = [
		"Saw", "White Noise", "Pink Noise", "Brown Noise", "Bass", "SillyVoice", "DefaultB",
		"Saw Combination", "Dominator", "Organ", "Pad", "Sax", "Cello", "Didgeridoo", "Pan flute"
	];

	carMenu.action = {
		if(pause == false,{ free(running); pause = true;});
		userCarrFile = false;
	};

	//Choose carrier file button
	carFileButton = Button(w);
	carFileButton.action = {
		if(pause == false,{ free(running); pause = true;});
		Dialog.openPanel({ arg path;
			carrFile = Buffer.read(s,path);
			carrier = {PlayBuf.ar(2, carrFile.bufnum, BufRateScale.kr(carrFile.bufnum), loop:1)};
			userCarrFile = true;
		},{
			"User cancelled the file choice".postln;
		});
	};

	carFileButton.string = "Choose file";

	//Disable file and use list
	useListButton = Button(w);
	useListButton.action = {
		if(pause == false,{ free(running); pause = true;});
		userCarrFile = false;
	};

	useListButton.string = "Use list";

	//Select modulator menu
	modMenuLabel = StaticText(w);
	modMenuLabel.string = "Modulator Signal";
	modMenuLabel.align = \center;

	//Use microphone button
	micButton = Button(w);
	micButton.string = "Microphone";
	micButton.action = {
		if(pause == false,{ free(running); pause = true;});
		modulator = {AudioIn.ar([1,2])};
	};

	//Choose modulator file button
	modFileButton = Button(w);
	modFileButton.action = {
		if(pause == false,{ free(running); pause = true;});
		Dialog.openPanel({ arg path;
			modFile = Buffer.read(s,path);
			modulator = {PlayBuf.ar(2, modFile.bufnum, BufRateScale.kr(modFile.bufnum), loop:1)};
		},{
			"User cancelled the file choice".postln;
		});
	};

	modFileButton.string = "Choose file";

	//Button to stop sound
	offButton = Button(w);
	offButton.action = {
		if(pause == false,{ free(running); pause = true;});
	};

	offButton.string = "Stop";
	//Button to start the sound
	onButton = Button(w);
	onButton.action = {
		//Stop the current playing audio if any
		if(pause == false,{ free(running);});
		pause = false;

		//We only do this if the carrier file is not user defined
		if(userCarrFile == false, {
			//The carrier signal has to be defined here because we need to be able to tune the frequency
			switch(carMenu.value,
				0, {carrier = {Saw.ar(freqKnob.value*freqMult+freqShift)}},
				1, {carrier = {WhiteNoise.ar()}},
				2, {carrier = {PinkNoise.ar()}},
				3, {carrier = {BrownNoise.ar()}},
				4, {carrier = {bass.value(freqKnob.value*freqMult+freqShift)}},
				5, {carrier = {sillyVoice.value(freqKnob.value*freqMult+freqShift)}},
				6, {carrier = {defaultB.value(freqKnob.value*freqMult+freqShift)}},
				7, {carrier = {sawCombination.value(freqKnob.value*freqMult+freqShift)}},
				8, {carrier = {dominator.value(freqKnob.value*freqMult+freqShift)}},
				9, {carrier = {PlayBuf.ar(2, organFile.bufnum, BufRateScale.kr(organFile.bufnum) / 123.47 *
					(freqKnob.value*freqMult+freqShift), loop:1)}},
				10, {carrier = {PlayBuf.ar(2, padFile.bufnum, BufRateScale.kr(padFile.bufnum) / 207.65 *
					(freqKnob.value*freqMult+freqShift), loop:1)}},
				11, {carrier = {PlayBuf.ar(2, saxFile.bufnum, BufRateScale.kr(saxFile.bufnum) / 185.00 *
					(freqKnob.value*freqMult+freqShift), loop:1)}},
				12, {carrier = {PlayBuf.ar(2, celloFile.bufnum, BufRateScale.kr(celloFile.bufnum) / 82.41 *
					(freqKnob.value*freqMult+freqShift), loop:1)}},
				13, {carrier = {PlayBuf.ar(2, didgFile.bufnum, BufRateScale.kr(didgFile.bufnum) / 110.0 *
					(freqKnob.value*freqMult+freqShift), loop:1)}},
				14, {carrier = {PlayBuf.ar(2, fluteFile.bufnum, BufRateScale.kr(fluteFile.bufnum) / 293.66 *
					(freqKnob.value*freqMult+freqShift), loop:1)}},
			);
		});

		running = vocoderIntuitive.value(carrier, modulator, round(bandsKnob.value*bandsMult + bandsShift),
			volKnob.value*volMult+volShift, fRangePow**freqRangeSlider.lo+fRangeShift,
			fRangePow**freqRangeSlider.hi+fRangeShift, bandWidthKnob.value*bandWidthMult+bandWidthShift).play;
	};
	onButton.string = "Play!";

	//Define an automatic layout
	w.layout = VLayout(
		HLayout(
			onButton,
			offButton),
		HLayout(
			VLayout(
				carMenuLabel,
				HLayout(useListButton, carMenu),
				carFileButton),
			VLayout(modMenuLabel, micButton, modFileButton)),
		HLayout(
			freqRangeSlider,
			VLayout(highFreqNr, fRangeLabel, lowFreqNr),
			VLayout(freqKnobLabel, freqKnob, freqKnobNr),
			VLayout(bandsKnobLabel, bandsKnob, bandsKnobNr),
			VLayout(bandWidthKnobLabel, bandWidthKnob, bandWidthKnobNr),
			VLayout(volKnobLabel, volKnob, volKnobNr)));
	w.front;
};

vocoderGUI.value;
)
