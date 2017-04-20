/*
Copyright 2017 Siemens AG.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.siemens.rl.interfaces;

import java.util.List;

/**
 * State/action description for the industrial benchmark
 */
public interface DataVectorDescription {

	/**
	 * Returns the number of variables.
	 * @return number of keys/dimensions
	 */
	int getNumberVariables();

	/**
	 * Returns a list containing the variable names.
	 * @return keys, in the same order as the dimensions in the data vector
	 */
	List<String> getVarNames();
}
