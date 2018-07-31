# gateplugin-DeepSRLAdapter
Integrate DeepSRL into GATE

This is a java wrapper to execute the [DeepSRL](https://github.com/luheng/deep_srl) approach, parse the STDOUT and add annotations to GATE as a Processing Resource.

It does not bundle DeepSRL, as it has its own [license](https://github.com/luheng/deep_srl/blob/master/LICENSE).


## Setup

* Follow the instructions on [DeepSRL](https://github.com/luheng/deep_srl) to fulfill the listed prerequisites. (The nltk and tcsh prerequisites are not needed)

* You will need to extract the models `conll05_model.tar.gz` and `conll05_propid_model.tar.gz` into the projects main folder `deep_srl-master`.

* Place the packaged script `gate_deepSRL.py` (in `src\main\python\...`) into the project folder `deep_srl-master\python`

## Parameters for DeepSRLAdapter

These are the parameters which have to be set when instantiating the DeepSRLAdapter as a processing resource in GATE.

* `deepSRLExecutable`: Path to the `gate_deepSRL.py` Script

* `modelPath`: Path to the `conll05_model`-folder

* `propidModelPath`: Path to the `conll05_propid_model`-folder

* `pythonExecutable`: Path to the Python2 executable